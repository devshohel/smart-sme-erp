package com.sme.erp.customer.receipt.service.impl;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.JournalStatus;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.customer.receipt.dto.CustomerReceiptDTO;
import com.sme.erp.customer.receipt.dto.CustomerReceiptPageDTO;
import com.sme.erp.customer.receipt.entity.CustomerReceipt;
import com.sme.erp.customer.receipt.enums.CustomerReceiptPaymentMethod;
import com.sme.erp.customer.receipt.enums.CustomerReceiptStatus;
import com.sme.erp.customer.receipt.mapper.CustomerReceiptMapper;
import com.sme.erp.customer.receipt.repository.CustomerReceiptRepository;
import com.sme.erp.customer.receipt.service.CustomerReceiptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerReceiptServiceImpl implements CustomerReceiptService {
    private static final String SOURCE_TYPE = "CUSTOMER_RECEIPT";

    private final CustomerReceiptRepository receiptRepository;
    private final CustomerRepository customerRepository;
    private final CustomerReceiptMapper mapper;
    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;

    public CustomerReceiptServiceImpl(CustomerReceiptRepository receiptRepository,
                                      CustomerRepository customerRepository,
                                      CustomerReceiptMapper mapper,
                                      JournalEntryRepository journalEntryRepository,
                                      AccountRepository accountRepository,
                                      ActivityLogService activityLogService,
                                      AuditLogService auditLogService) {
        this.receiptRepository = receiptRepository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.journalEntryRepository = journalEntryRepository;
        this.accountRepository = accountRepository;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerReceiptDTO> getAll() {
        return receiptRepository.findAll().stream()
                .sorted((left, right) -> right.getId().compareTo(left.getId()))
                .map(this::withJournalInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerReceiptPageDTO searchPage(String keyword, Long customerId, CustomerReceiptStatus status,
                                             CustomerReceiptPaymentMethod paymentMethod, LocalDate fromDate, LocalDate toDate,
                                             int page, int size, String sort, String direction) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize(size), sortFor(sort, direction));
        Page<CustomerReceipt> result = receiptRepository.searchPage(
                RequestValueUtils.normalize(keyword),
                customerId,
                status,
                paymentMethod,
                fromDate,
                toDate,
                pageable);
        return new CustomerReceiptPageDTO(
                result.getContent().stream().map(this::withJournalInfo).collect(Collectors.toList()),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerReceiptDTO getById(Long id) {
        return withJournalInfo(findReceiptById(id));
    }

    @Override
    @Transactional
    public CustomerReceiptDTO create(CustomerReceiptDTO dto) {
        normalize(dto);
        validateCreate(dto);

        CustomerReceipt receipt = new CustomerReceipt();
        receipt.setReceiptNo(resolveReceiptNo());
        receipt.setCustomer(findCustomerById(dto.getCustomerId()));
        receipt.setReceiptDate(dto.getReceiptDate());
        receipt.setPaymentMethod(dto.getPaymentMethod());
        receipt.setAmount(dto.getAmount());
        receipt.setReferenceNo(RequestValueUtils.normalize(dto.getReferenceNo()));
        receipt.setNotes(RequestValueUtils.normalize(dto.getNotes()));
        receipt.setStatus(CustomerReceiptStatus.DRAFT);

        CustomerReceiptDTO saved = withJournalInfo(receiptRepository.save(receipt));
        activityLogService.log("CUSTOMER_RECEIPT_CREATE", "CUSTOMER", "customer_receipts", saved.getId(), "Created receipt " + saved.getReceiptNo());
        auditLogService.log("customer_receipts", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public CustomerReceiptDTO update(Long id, CustomerReceiptDTO dto) {
        CustomerReceipt receipt = findReceiptById(id);
        ensureEditable(receipt);
        normalize(dto);
        validateCreate(dto);
        CustomerReceiptDTO oldData = withJournalInfo(receipt);

        receipt.setCustomer(findCustomerById(dto.getCustomerId()));
        receipt.setReceiptDate(dto.getReceiptDate());
        receipt.setPaymentMethod(dto.getPaymentMethod());
        receipt.setAmount(dto.getAmount());
        receipt.setReferenceNo(RequestValueUtils.normalize(dto.getReferenceNo()));
        receipt.setNotes(RequestValueUtils.normalize(dto.getNotes()));

        CustomerReceiptDTO saved = withJournalInfo(receiptRepository.save(receipt));
        auditLogService.log("customer_receipts", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public CustomerReceiptDTO post(Long id) {
        CustomerReceipt receipt = findReceiptById(id);
        if (receipt.getStatus() == CustomerReceiptStatus.POSTED) {
            return withJournalInfo(receipt);
        }
        if (receipt.getStatus() == CustomerReceiptStatus.CANCELLED) {
            throw new BadRequestException("Cancelled receipt cannot be posted.");
        }

        if (!journalEntryRepository.existsBySourceTypeAndSourceId(SOURCE_TYPE, id)) {
            JournalEntry journalEntry = buildJournalEntry(receipt);
            saveIfBalanced(journalEntry);
        }

        receipt.setStatus(CustomerReceiptStatus.POSTED);
        receipt.setPostedAt(LocalDateTime.now());
        CustomerReceiptDTO saved = withJournalInfo(receiptRepository.save(receipt));
        activityLogService.log("CUSTOMER_RECEIPT_POST", "ACCOUNTING", "customer_receipts", saved.getId(), "Posted receipt " + saved.getReceiptNo());
        auditLogService.log("customer_receipts", saved.getId(), null, auditLogService.toJson(saved), "POST");
        return saved;
    }

    @Override
    @Transactional
    public CustomerReceiptDTO cancel(Long id) {
        CustomerReceipt receipt = findReceiptById(id);
        if (receipt.getStatus() == CustomerReceiptStatus.POSTED) {
            throw new BadRequestException("Posted receipt cancellation requires reversal workflow and is not available in Batch-2A.");
        }
        if (receipt.getStatus() == CustomerReceiptStatus.CANCELLED) {
            throw new BadRequestException("Receipt is already cancelled.");
        }

        receipt.setStatus(CustomerReceiptStatus.CANCELLED);
        receipt.setCancelledAt(LocalDateTime.now());
        CustomerReceiptDTO saved = withJournalInfo(receiptRepository.save(receipt));
        activityLogService.log("CUSTOMER_RECEIPT_CANCEL", "CUSTOMER", "customer_receipts", saved.getId(), "Cancelled receipt " + saved.getReceiptNo());
        auditLogService.log("customer_receipts", saved.getId(), null, auditLogService.toJson(saved), "CANCEL");
        return saved;
    }

    private CustomerReceiptDTO withJournalInfo(CustomerReceipt receipt) {
        CustomerReceiptDTO dto = mapper.toDTO(receipt);
        journalEntryRepository.findBySource(SOURCE_TYPE, receipt.getId())
                .ifPresent(entry -> dto.setJournalNo(entry.getJournalNo()));
        return dto;
    }

    private JournalEntry buildJournalEntry(CustomerReceipt receipt) {
        Account debitAccount = receipt.getPaymentMethod() == CustomerReceiptPaymentMethod.BANK
                || receipt.getPaymentMethod() == CustomerReceiptPaymentMethod.MOBILE_BANKING
                || receipt.getPaymentMethod() == CustomerReceiptPaymentMethod.CHEQUE
                ? account("Bank")
                : account("Cash");

        JournalEntry entry = new JournalEntry();
        entry.setJournalNo(nextJournalNo());
        entry.setJournalDate(receipt.getReceiptDate());
        entry.setReferenceNo(receipt.getReceiptNo());
        entry.setDescription("Customer receipt posting " + receipt.getReceiptNo());
        entry.setSourceType(SOURCE_TYPE);
        entry.setSourceId(receipt.getId());
        entry.setSourceNo(receipt.getReceiptNo());
        entry.setStatus(JournalStatus.POSTED);

        addLine(entry, debitAccount, receipt.getAmount(), BigDecimal.ZERO, "Customer receipt");
        addLine(entry, account("Accounts Receivable"), BigDecimal.ZERO, receipt.getAmount(), "Receivable reduction");
        return entry;
    }

    private void addLine(JournalEntry entry, Account account, BigDecimal debit, BigDecimal credit, String description) {
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account);
        line.setDebit(debit == null ? BigDecimal.ZERO : debit);
        line.setCredit(credit == null ? BigDecimal.ZERO : credit);
        line.setDescription(description);
        entry.addLine(line);
    }

    private void saveIfBalanced(JournalEntry entry) {
        BigDecimal debit = entry.getLines().stream().map(JournalEntryLine::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credit = entry.getLines().stream().map(JournalEntryLine::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (debit.signum() <= 0 || debit.compareTo(credit) != 0) {
            throw new BadRequestException("Receipt journal entry is not balanced.");
        }
        journalEntryRepository.save(entry);
    }

    private Account account(String name) {
        return accountRepository.findByAccountNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Required account not found: " + name));
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private CustomerReceipt findReceiptById(Long id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer receipt not found with id: " + id));
    }

    private void validateCreate(CustomerReceiptDTO dto) {
        if (dto.getCustomerId() == null) {
            throw new BadRequestException("Customer is required");
        }
        if (dto.getReceiptDate() == null) {
            throw new BadRequestException("Receipt date is required");
        }
        if (dto.getPaymentMethod() == null) {
            throw new BadRequestException("Payment method is required");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
    }

    private void ensureEditable(CustomerReceipt receipt) {
        if (receipt.getStatus() == CustomerReceiptStatus.POSTED) {
            throw new BadRequestException("Posted receipt cannot be edited.");
        }
        if (receipt.getStatus() == CustomerReceiptStatus.CANCELLED) {
            throw new BadRequestException("Cancelled receipt cannot be edited.");
        }
    }

    private void normalize(CustomerReceiptDTO dto) {
        dto.setReferenceNo(RequestValueUtils.normalize(dto.getReferenceNo()));
        dto.setNotes(RequestValueUtils.normalize(dto.getNotes()));
    }

    private String resolveReceiptNo() {
        long nextNumber = receiptRepository.findTopByOrderByIdDesc()
                .map(receipt -> receipt.getId() + 1)
                .orElse(1L);
        String generated = String.format("CR-%06d", nextNumber);
        while (receiptRepository.existsByReceiptNo(generated)) {
            nextNumber++;
            generated = String.format("CR-%06d", nextNumber);
        }
        return generated;
    }

    private Sort sortFor(String sort, String direction) {
        String property = switch (sort == null ? "" : sort) {
            case "receiptNo" -> "receiptNo";
            case "customerName" -> "customer.name";
            case "customerCode" -> "customer.customerCode";
            case "receiptDate" -> "receiptDate";
            case "amount" -> "amount";
            case "paymentMethod" -> "paymentMethod";
            case "status" -> "status";
            case "postedAt" -> "postedAt";
            default -> "receiptDate";
        };
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, property).and(Sort.by(Sort.Direction.DESC, "id"));
    }

    private int safeSize(int size) {
        return size == 25 || size == 50 || size == 100 ? size : 10;
    }

    private String nextJournalNo() {
        long next = journalEntryRepository.findMaxId() + 1;
        String value = String.format("JRN-%04d", next);
        while (journalEntryRepository.existsByJournalNo(value)) {
            next++;
            value = String.format("JRN-%04d", next);
        }
        return value;
    }
}
