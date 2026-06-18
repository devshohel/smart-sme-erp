package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.dto.ExpenseDTO;
import com.sme.erp.accounting.dto.ExpensePageDTO;
import com.sme.erp.accounting.dto.ExpenseReportRowDTO;
import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.accounting.entity.ExpenseCategory;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;
import com.sme.erp.accounting.mapper.AccountingMapper;
import com.sme.erp.accounting.repository.ExpenseCategoryRepository;
import com.sme.erp.accounting.repository.ExpenseRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.accounting.service.ExpenseService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.enums.Status;
import com.sme.erp.file.dto.StoredFileDTO;
import com.sme.erp.file.service.FileStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseRepository repository;
    private final ExpenseCategoryRepository categoryRepository;
    private final AccountingMapper mapper;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private final AccountingPostingService accountingPostingService;
    private final JournalEntryRepository journalEntryRepository;
    private final FileStorageService fileStorageService;

    public ExpenseServiceImpl(ExpenseRepository repository, ExpenseCategoryRepository categoryRepository, AccountingMapper mapper, ActivityLogService activityLogService, AuditLogService auditLogService, AccountingPostingService accountingPostingService, JournalEntryRepository journalEntryRepository, FileStorageService fileStorageService) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.accountingPostingService = accountingPostingService;
        this.journalEntryRepository = journalEntryRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDTO> getAll(LocalDate fromDate, LocalDate toDate, Long categoryId, AccountingPaymentMethod paymentMethod) {
        return repository.search(fromDate, toDate, categoryId, paymentMethod).stream().map(mapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpensePageDTO searchPage(String keyword, LocalDate fromDate, LocalDate toDate, Long categoryId,
                                     AccountingPaymentMethod paymentMethod, ExpenseStatus status,
                                     int page, int size, String sort, String direction) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("From date cannot be after to date.");
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), buildSort(sort, direction));
        Page<Expense> result = repository.searchPage(RequestValueUtils.normalize(keyword), fromDate, toDate,
                categoryId, paymentMethod, status, pageable);
        return new ExpensePageDTO(
                result.getContent().stream().map(this::toDTO).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDTO> approvalQueue(LocalDate fromDate, LocalDate toDate, Long categoryId, String submittedBy,
                                          BigDecimal amountMin, BigDecimal amountMax) {
        validateDateRange(fromDate, toDate);
        if (amountMin != null && amountMax != null && amountMin.compareTo(amountMax) > 0) {
            throw new BadRequestException("Minimum amount cannot exceed maximum amount");
        }
        return repository.approvalQueue(fromDate, toDate, categoryId, RequestValueUtils.normalize(submittedBy),
                amountMin, amountMax).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO getById(Long id) {
        return toDTO(find(id));
    }

    @Override
    @Transactional
    public ExpenseDTO create(ExpenseDTO dto) {
        return create(dto, null);
    }

    @Override
    @Transactional
    public ExpenseDTO create(ExpenseDTO dto, MultipartFile receipt) {
        Expense expense = new Expense();
        expense.setExpenseNo(nextExpenseNo());
        expense.setCreatedBy(currentUsername());
        apply(dto, expense);
        expense.setStatus(ExpenseStatus.DRAFT);
        applyReceiptIfPresent(expense, receipt);
        Expense savedEntity = repository.save(expense);
        ExpenseDTO saved = toDTO(savedEntity);
        activityLogService.log("EXPENSE_CREATE", "ACCOUNTING", "accounting_expenses", saved.getId(), "Created expense " + saved.getExpenseNo());
        if (receipt != null && !receipt.isEmpty()) {
            activityLogService.log("EXPENSE_ATTACHMENT_UPLOAD", "ACCOUNTING", "accounting_expenses", saved.getId(), "Uploaded receipt for " + saved.getExpenseNo());
        }
        auditLogService.log("accounting_expenses", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO update(Long id, ExpenseDTO dto) {
        return update(id, dto, null);
    }

    @Override
    @Transactional
    public ExpenseDTO update(Long id, ExpenseDTO dto, MultipartFile receipt) {
        Expense expense = find(id);
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Only draft expenses can be edited");
        }
        ExpenseDTO oldData = toDTO(expense);
        apply(dto, expense);
        expense.setStatus(ExpenseStatus.DRAFT);
        applyReceiptIfPresent(expense, receipt);
        ExpenseDTO saved = toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_UPDATE", "ACCOUNTING", "accounting_expenses", saved.getId(), "Updated expense " + saved.getExpenseNo());
        if (receipt != null && !receipt.isEmpty()) {
            activityLogService.log("EXPENSE_ATTACHMENT_UPLOAD", "ACCOUNTING", "accounting_expenses", saved.getId(), "Uploaded receipt for " + saved.getExpenseNo());
        }
        auditLogService.log("accounting_expenses", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO submit(Long id) {
        Expense expense = find(id);
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Only draft expenses can be submitted");
        }
        ExpenseDTO oldData = toDTO(expense);
        expense.setStatus(ExpenseStatus.SUBMITTED);
        expense.setSubmittedAt(LocalDateTime.now());
        expense.setSubmittedBy(currentUsername());
        ExpenseDTO saved = toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_SUBMIT", "ACCOUNTING", "accounting_expenses", saved.getId(), "Submitted expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "SUBMIT");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO approve(Long id) {
        Expense expense = find(id);
        if (expense.getStatus() == ExpenseStatus.APPROVED || expense.getStatus() == ExpenseStatus.POSTED) {
            throw new BadRequestException("Expense is already approved");
        }
        if (expense.getStatus() != ExpenseStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted expenses can be approved");
        }
        ExpenseDTO oldData = toDTO(expense);
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setApprovedAt(LocalDateTime.now());
        expense.setApprovedBy(currentUsername());
        expense.setApprovalComment("Approved");
        ExpenseDTO saved = toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_APPROVE", "ACCOUNTING", "accounting_expenses", saved.getId(), "Approved expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "APPROVE");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO reject(Long id, String reason) {
        Expense expense = find(id);
        if (expense.getStatus() == ExpenseStatus.REJECTED) {
            throw new BadRequestException("Expense is already rejected");
        }
        if (expense.getStatus() != ExpenseStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted expenses can be rejected");
        }
        String normalizedReason = RequestValueUtils.normalizeRequired(reason, "Rejection reason");
        ExpenseDTO oldData = toDTO(expense);
        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setRejectedAt(LocalDateTime.now());
        expense.setRejectedBy(currentUsername());
        expense.setRejectionReason(normalizedReason);
        ExpenseDTO saved = toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_REJECT", "ACCOUNTING", "accounting_expenses", saved.getId(), "Rejected expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "REJECT");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO cancel(Long id) {
        Expense expense = find(id);
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Only draft expenses can be cancelled");
        }
        ExpenseDTO oldData = toDTO(expense);
        expense.setStatus(ExpenseStatus.CANCELLED);
        ExpenseDTO saved = toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_CANCEL", "ACCOUNTING", "accounting_expenses", saved.getId(), "Cancelled expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "CANCEL");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO post(Long id) {
        Expense expense = find(id);
        if (expense.getStatus() == ExpenseStatus.POSTED || journalEntryRepository.existsBySourceTypeAndSourceId("EXPENSE", id)) {
            throw new BadRequestException("Expense is already posted");
        }
        if (expense.getStatus() != ExpenseStatus.APPROVED) {
            throw new BadRequestException("Only approved expenses can be posted");
        }
        if (expense.getCategory() == null || expense.getCategory().getAccount() == null) {
            throw new BadRequestException("Expense category must have a mapped GL account before posting");
        }
        accountingPostingService.postExpense(expense);
        expense.setStatus(ExpenseStatus.POSTED);
        ExpenseDTO saved = toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_POST", "ACCOUNTING", "accounting_expenses", saved.getId(), "Posted expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), null, auditLogService.toJson(saved), "POST");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO reverse(Long id, String reversalReason) {
        Expense expense = find(id);
        if (expense.getStatus() == ExpenseStatus.REVERSED || journalEntryRepository.existsBySourceTypeAndSourceId("EXPENSE_REVERSAL", id)) {
            throw new BadRequestException("Expense is already reversed");
        }
        if (expense.getStatus() != ExpenseStatus.POSTED) {
            throw new BadRequestException("Only posted expenses can be reversed");
        }
        String normalizedReason = RequestValueUtils.normalizeRequired(reversalReason, "Reversal reason");
        ExpenseDTO oldData = toDTO(expense);
        accountingPostingService.reverseExpense(expense, normalizedReason);
        expense.setStatus(ExpenseStatus.REVERSED);
        expense.setReversedAt(LocalDateTime.now());
        expense.setReversedBy(currentUsername());
        expense.setReversalReason(normalizedReason);
        ExpenseDTO saved = toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_REVERSE", "ACCOUNTING", "accounting_expenses", saved.getId(), "Reversed expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "REVERSE");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseReportRowDTO> reportSummary(LocalDate fromDate, LocalDate toDate, Long categoryId, ExpenseStatus status) {
        List<Expense> rows = reportExpenses(fromDate, toDate, categoryId, status);
        return List.of(new ExpenseReportRowDTO("Summary", sumNet(rows), sumTax(rows), sumGross(rows), rows.size()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseReportRowDTO> reportByCategory(LocalDate fromDate, LocalDate toDate, Long categoryId, ExpenseStatus status) {
        return groupedReport(reportExpenses(fromDate, toDate, categoryId, status),
                expense -> expense.getCategory() != null ? expense.getCategory().getName() : "Uncategorized");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseReportRowDTO> reportByPaymentMethod(LocalDate fromDate, LocalDate toDate, Long categoryId, ExpenseStatus status) {
        return groupedReport(reportExpenses(fromDate, toDate, categoryId, status),
                expense -> expense.getPaymentMethod() != null ? expense.getPaymentMethod().name() : "N/A");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseReportRowDTO> reportTax(LocalDate fromDate, LocalDate toDate, Long categoryId, ExpenseStatus status) {
        return groupedReport(reportExpenses(fromDate, toDate, categoryId, status).stream()
                        .filter(expense -> safe(expense.getTaxAmount()).signum() > 0).toList(),
                expense -> "Taxable Expenses");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseReportRowDTO> reportMonthly(LocalDate fromDate, LocalDate toDate, Long categoryId, ExpenseStatus status) {
        return groupedReport(reportExpenses(fromDate, toDate, categoryId, status),
                expense -> YearMonth.from(expense.getExpenseDate()).toString());
    }

    private void apply(ExpenseDTO dto, Expense expense) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        ExpenseCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense category not found with id: " + dto.getCategoryId()));
        if (category.getStatus() != Status.ACTIVE) {
            throw new BadRequestException("Expense category must be active");
        }
        expense.setExpenseDate(dto.getExpenseDate() != null ? dto.getExpenseDate() : LocalDate.now());
        expense.setCategory(category);
        expense.setAmount(dto.getAmount());
        expense.setPaymentMethod(dto.getPaymentMethod());
        expense.setReferenceNo(RequestValueUtils.normalize(dto.getReferenceNo()));
        expense.setNotes(RequestValueUtils.normalize(dto.getNotes()));
        applyTax(dto, expense);
    }

    private void applyTax(ExpenseDTO dto, Expense expense) {
        boolean taxApplicable = Boolean.TRUE.equals(dto.getTaxApplicable());
        BigDecimal base = positive(dto.getNetAmount()) ? dto.getNetAmount() : dto.getAmount();
        BigDecimal rate = taxApplicable ? safe(dto.getTaxRate()) : BigDecimal.ZERO;
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Tax rate cannot be negative");
        }
        BigDecimal netAmount = safe(base).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = taxApplicable
                ? netAmount.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal grossAmount = netAmount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        expense.setTaxApplicable(taxApplicable);
        expense.setTaxRate(rate);
        expense.setNetAmount(netAmount);
        expense.setTaxAmount(taxAmount);
        expense.setGrossAmount(grossAmount);
        expense.setAmount(grossAmount);
    }

    private void applyReceiptIfPresent(Expense expense, MultipartFile receipt) {
        if (receipt == null || receipt.isEmpty()) {
            return;
        }
        StoredFileDTO storedFile = fileStorageService.storeExpenseReceipt(receipt);
        expense.setReceiptOriginalFilename(storedFile.getOriginalFilename());
        expense.setReceiptStoredFilename(storedFile.getStoredFilename());
        expense.setReceiptContentType(storedFile.getContentType());
        expense.setReceiptSize(storedFile.getFileSize());
        expense.setReceiptPath(storedFile.getStoragePath());
        expense.setReceiptUrl(storedFile.getPublicUrl());
    }

    private String nextExpenseNo() {
        long next = repository.findMaxId() + 1;
        String value = String.format("EXP-%04d", next);
        while (repository.existsByExpenseNo(value)) {
            next++;
            value = String.format("EXP-%04d", next);
        }
        return value;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    private Expense find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    private ExpenseDTO toDTO(Expense expense) {
        ExpenseDTO dto = mapper.toDTO(expense);
        if (dto != null && expense.getId() != null) {
            journalEntryRepository.findBySource("EXPENSE", expense.getId()).ifPresent(journal -> dto.setJournalEntryId(journal.getId()));
        }
        return dto;
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("From date cannot be after to date.");
        }
    }

    private List<Expense> reportExpenses(LocalDate fromDate, LocalDate toDate, Long categoryId, ExpenseStatus status) {
        validateDateRange(fromDate, toDate);
        return repository.search(fromDate, toDate, categoryId, null).stream()
                .filter(expense -> status == null || expense.getStatus() == status)
                .toList();
    }

    private List<ExpenseReportRowDTO> groupedReport(List<Expense> expenses, java.util.function.Function<Expense, String> classifier) {
        Map<String, List<Expense>> groups = new LinkedHashMap<>();
        for (Expense expense : expenses) {
            groups.computeIfAbsent(classifier.apply(expense), key -> new java.util.ArrayList<>()).add(expense);
        }
        return groups.entrySet().stream()
                .map(entry -> new ExpenseReportRowDTO(entry.getKey(), sumNet(entry.getValue()), sumTax(entry.getValue()), sumGross(entry.getValue()), entry.getValue().size()))
                .toList();
    }

    private BigDecimal sumNet(List<Expense> expenses) {
        return expenses.stream().map(expense -> positive(expense.getNetAmount()) ? expense.getNetAmount() : expense.getAmount()).map(this::safe).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumTax(List<Expense> expenses) {
        return expenses.stream().map(Expense::getTaxAmount).map(this::safe).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumGross(List<Expense> expenses) {
        return expenses.stream().map(expense -> positive(expense.getGrossAmount()) ? expense.getGrossAmount() : expense.getAmount()).map(this::safe).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean positive(BigDecimal value) {
        return safe(value).signum() > 0;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Sort buildSort(String sort, String direction) {
        Set<String> allowed = Set.of("id", "expenseNo", "expenseDate", "amount", "paymentMethod", "status", "createdAt");
        String property = allowed.contains(sort) ? sort : "expenseDate";
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, property).and(Sort.by(Sort.Direction.DESC, "id"));
    }
}
