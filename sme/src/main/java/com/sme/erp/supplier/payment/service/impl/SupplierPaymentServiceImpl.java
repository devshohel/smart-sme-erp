package com.sme.erp.supplier.payment.service.impl;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.JournalStatus;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.audit.service.impl.CurrentAuditUser;
import com.sme.erp.auth.entity.User;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.payment.dto.SupplierPaymentAllocationDTO;
import com.sme.erp.supplier.payment.dto.SupplierPaymentDTO;
import com.sme.erp.supplier.payment.dto.SupplierPaymentPageDTO;
import com.sme.erp.supplier.payment.entity.SupplierPayment;
import com.sme.erp.supplier.payment.entity.SupplierPaymentAllocation;
import com.sme.erp.supplier.payment.enums.SupplierPaymentAllocationMode;
import com.sme.erp.supplier.payment.enums.SupplierPaymentMethod;
import com.sme.erp.supplier.payment.enums.SupplierPaymentStatus;
import com.sme.erp.supplier.payment.mapper.SupplierPaymentMapper;
import com.sme.erp.supplier.payment.repository.SupplierPaymentRepository;
import com.sme.erp.supplier.payment.service.SupplierPaymentService;
import com.sme.erp.supplier.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupplierPaymentServiceImpl implements SupplierPaymentService {
    private com.sme.erp.accounting.service.AccountingPeriodService periodService;
    @org.springframework.beans.factory.annotation.Autowired public void setPeriodService(com.sme.erp.accounting.service.AccountingPeriodService s){periodService=s;}
    private static final String SOURCE_TYPE = "SUPPLIER_PAYMENT";
    private static final String REVERSAL_SOURCE_TYPE = "SUPPLIER_PAYMENT_REVERSAL";

    private final SupplierPaymentRepository paymentRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierPaymentMapper mapper;
    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private final CurrentAuditUser currentAuditUser;

    public SupplierPaymentServiceImpl(SupplierPaymentRepository paymentRepository,
                                      SupplierRepository supplierRepository,
                                      PurchaseOrderRepository purchaseOrderRepository,
                                      SupplierPaymentMapper mapper,
                                      JournalEntryRepository journalEntryRepository,
                                      AccountRepository accountRepository,
                                      ActivityLogService activityLogService,
                                      AuditLogService auditLogService,
                                      CurrentAuditUser currentAuditUser) {
        this.paymentRepository = paymentRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.mapper = mapper;
        this.journalEntryRepository = journalEntryRepository;
        this.accountRepository = accountRepository;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.currentAuditUser = currentAuditUser;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierPaymentDTO> getAll() {
        return paymentRepository.findAll().stream()
                .sorted((left, right) -> right.getId().compareTo(left.getId()))
                .map(payment -> withJournalInfo(payment, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierPaymentPageDTO searchPage(String keyword, Long supplierId, SupplierPaymentStatus status,
                                             SupplierPaymentMethod paymentMethod, LocalDate fromDate, LocalDate toDate,
                                             int page, int size, String sort, String direction) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize(size), sortFor(sort, direction));
        Page<SupplierPayment> result = paymentRepository.searchPage(
                RequestValueUtils.normalize(keyword),
                supplierId,
                status,
                paymentMethod,
                fromDate,
                toDate,
                pageable);
        return new SupplierPaymentPageDTO(
                result.getContent().stream().map(payment -> withJournalInfo(payment, false)).collect(Collectors.toList()),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierPaymentDTO getById(Long id) {
        return withJournalInfo(findPaymentByIdDetailed(id), true);
    }

    @Override
    @Transactional
    public SupplierPaymentDTO create(SupplierPaymentDTO dto) {
        normalize(dto);
        validateCreate(dto);

        SupplierPayment payment = new SupplierPayment();
        populatePayment(payment, dto);
        payment.setPaymentNo(resolvePaymentNo());
        payment.setStatus(SupplierPaymentStatus.DRAFT);
        applyDraftAllocations(payment, dto);

        SupplierPaymentDTO saved = withJournalInfo(paymentRepository.save(payment), true);
        activityLogService.log("SUPPLIER_PAYMENT_CREATE", "SUPPLIER", "supplier_payments", saved.getId(), "Created supplier payment " + saved.getPaymentNo());
        auditLogService.log("supplier_payments", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public SupplierPaymentDTO update(Long id, SupplierPaymentDTO dto) {
        SupplierPayment payment = findPaymentByIdDetailed(id);
        ensureEditable(payment);
        normalize(dto);
        validateCreate(dto);
        SupplierPaymentDTO oldData = withJournalInfo(payment, true);

        populatePayment(payment, dto);
        applyDraftAllocations(payment, dto);

        SupplierPaymentDTO saved = withJournalInfo(paymentRepository.save(payment), true);
        auditLogService.log("supplier_payments", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public SupplierPaymentDTO post(Long id) {
        SupplierPayment payment = findPaymentByIdDetailed(id);
        if(periodService!=null) periodService.assertOpen(payment.getPaymentDate());
        if (payment.getStatus() == SupplierPaymentStatus.POSTED) {
            throw new BadRequestException("Supplier payment is already posted.");
        }
        if (payment.getStatus() == SupplierPaymentStatus.CANCELLED) {
            throw new BadRequestException("Cancelled supplier payment cannot be posted.");
        }

        AllocationPlan plan = resolveAllocationPlan(payment, true);
        applyAllocationPlan(payment, plan);

        payment.setTotalAllocatedAmount(plan.totalAllocatedAmount());
        payment.setUnappliedAmount(plan.unappliedAmount());
        if (!journalEntryRepository.existsBySourceTypeAndSourceId(SOURCE_TYPE, id)) {
            saveIfBalanced(buildJournalEntry(payment));
        }

        payment.setStatus(SupplierPaymentStatus.POSTED);
        payment.setPostedAt(LocalDateTime.now());
        SupplierPaymentDTO saved = withJournalInfo(paymentRepository.save(payment), true);
        activityLogService.log("SUPPLIER_PAYMENT_POST", "ACCOUNTING", "supplier_payments", saved.getId(), "Posted supplier payment " + saved.getPaymentNo());
        auditLogService.log("supplier_payments", saved.getId(), null, auditLogService.toJson(saved), "POST");
        return saved;
    }

    @Override
    @Transactional
    public SupplierPaymentDTO reverse(Long id, String reversalReason) {
        SupplierPayment payment = findPaymentByIdDetailed(id);
        if(periodService!=null) periodService.assertOpen(payment.getPaymentDate());
        if (payment.getStatus() == SupplierPaymentStatus.DRAFT) {
            throw new BadRequestException("Draft supplier payment cannot be reversed.");
        }
        if (payment.getStatus() == SupplierPaymentStatus.CANCELLED) {
            throw new BadRequestException("Cancelled supplier payment cannot be reversed.");
        }
        if (payment.getStatus() == SupplierPaymentStatus.REVERSED || payment.getReversedAt() != null
                || journalEntryRepository.existsBySourceTypeAndSourceId(REVERSAL_SOURCE_TYPE, id)) {
            throw new BadRequestException("Supplier payment is already reversed.");
        }
        if (payment.getStatus() != SupplierPaymentStatus.POSTED) {
            throw new BadRequestException("Only posted supplier payments can be reversed.");
        }

        rollbackAllocations(payment);
        saveIfBalanced(buildReversalJournalEntry(payment));
        payment.setStatus(SupplierPaymentStatus.REVERSED);
        payment.setReversedAt(LocalDateTime.now());
        payment.setReversalReason(RequestValueUtils.normalize(reversalReason));
        User currentUser = currentAuditUser.currentUserOrNull();
        if (currentUser != null) {
            payment.setReversedBy(currentUser.getId());
        }

        SupplierPaymentDTO saved = withJournalInfo(paymentRepository.save(payment), true);
        activityLogService.log("SUPPLIER_PAYMENT_REVERSE", "ACCOUNTING", "supplier_payments", saved.getId(), "Reversed supplier payment " + saved.getPaymentNo());
        auditLogService.log("supplier_payments", saved.getId(), null, auditLogService.toJson(saved), "REVERSE");
        return saved;
    }

    @Override
    @Transactional
    public SupplierPaymentDTO cancel(Long id) {
        SupplierPayment payment = findPaymentByIdDetailed(id);
        if(periodService!=null) periodService.assertOpen(payment.getPaymentDate());
        if (payment.getStatus() == SupplierPaymentStatus.POSTED) {
            throw new BadRequestException("Posted supplier payment cancellation requires reversal workflow and is not available in Batch-2.");
        }
        if (payment.getStatus() == SupplierPaymentStatus.CANCELLED) {
            throw new BadRequestException("Supplier payment is already cancelled.");
        }
        payment.setStatus(SupplierPaymentStatus.CANCELLED);
        payment.setCancelledAt(LocalDateTime.now());
        SupplierPaymentDTO saved = withJournalInfo(paymentRepository.save(payment), true);
        activityLogService.log("SUPPLIER_PAYMENT_CANCEL", "SUPPLIER", "supplier_payments", saved.getId(), "Cancelled supplier payment " + saved.getPaymentNo());
        auditLogService.log("supplier_payments", saved.getId(), null, auditLogService.toJson(saved), "CANCEL");
        return saved;
    }

    private SupplierPaymentDTO withJournalInfo(SupplierPayment payment, boolean includeAllocations) {
        SupplierPaymentDTO dto = mapper.toDTO(payment, includeAllocations);
        journalEntryRepository.findBySource(SOURCE_TYPE, payment.getId())
                .ifPresent(entry -> dto.setJournalNo(entry.getJournalNo()));
        return dto;
    }

    private void populatePayment(SupplierPayment payment, SupplierPaymentDTO dto) {
        payment.setSupplier(findSupplierById(dto.getSupplierId()));
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setAmount(dto.getAmount());
        payment.setReferenceNo(RequestValueUtils.normalize(dto.getReferenceNo()));
        payment.setNotes(RequestValueUtils.normalize(dto.getNotes()));
        payment.setAllocationMode(dto.getAllocationMode() != null ? dto.getAllocationMode() : SupplierPaymentAllocationMode.AUTO);
    }

    private void applyDraftAllocations(SupplierPayment payment, SupplierPaymentDTO dto) {
        AllocationPlan plan = resolveAllocationPlan(payment, false, dto.getAllocations());
        payment.getAllocations().clear();
        payment.getAllocations().addAll(plan.allocations().stream().map(allocation -> {
            allocation.setSupplierPayment(payment);
            return allocation;
        }).collect(Collectors.toList()));
        payment.setTotalAllocatedAmount(plan.totalAllocatedAmount());
        payment.setUnappliedAmount(plan.unappliedAmount());
    }

    private AllocationPlan resolveAllocationPlan(SupplierPayment payment, boolean applyToPurchases) {
        return resolveAllocationPlan(payment, applyToPurchases, null);
    }

    private AllocationPlan resolveAllocationPlan(SupplierPayment payment, boolean applyToPurchases, List<SupplierPaymentAllocationDTO> requestedAllocations) {
        SupplierPaymentAllocationMode mode = payment.getAllocationMode() != null ? payment.getAllocationMode() : SupplierPaymentAllocationMode.AUTO;
        if (mode == SupplierPaymentAllocationMode.MANUAL && requestedAllocations != null) {
            return buildManualPlan(payment, requestedAllocations, applyToPurchases);
        }
        if (mode == SupplierPaymentAllocationMode.MANUAL && requestedAllocations == null && payment.getAllocations() != null && !payment.getAllocations().isEmpty()) {
            return buildManualPlanFromEntities(payment, applyToPurchases);
        }
        if (mode == SupplierPaymentAllocationMode.MANUAL) {
            return new AllocationPlan(new ArrayList<>(), BigDecimal.ZERO, safe(payment.getAmount()));
        }
        return buildAutoPlan(payment, applyToPurchases);
    }

    private AllocationPlan buildManualPlan(SupplierPayment payment, List<SupplierPaymentAllocationDTO> requestedAllocations, boolean applyToPurchases) {
        Map<Long, BigDecimal> consolidated = new LinkedHashMap<>();
        for (SupplierPaymentAllocationDTO allocationDTO : requestedAllocations) {
            if (allocationDTO == null || allocationDTO.getPurchaseOrderId() == null || allocationDTO.getAllocatedAmount() == null) {
                continue;
            }
            BigDecimal amount = safe(allocationDTO.getAllocatedAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            consolidated.merge(allocationDTO.getPurchaseOrderId(), amount, BigDecimal::add);
        }
        return buildPlanFromPurchaseMap(payment, consolidated, applyToPurchases);
    }

    private AllocationPlan buildManualPlanFromEntities(SupplierPayment payment, boolean applyToPurchases) {
        Map<Long, BigDecimal> consolidated = new LinkedHashMap<>();
        for (SupplierPaymentAllocation allocation : payment.getAllocations()) {
            if (allocation.getPurchaseOrder() == null || allocation.getAllocatedAmount() == null) {
                continue;
            }
            BigDecimal amount = safe(allocation.getAllocatedAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            consolidated.merge(allocation.getPurchaseOrder().getId(), amount, BigDecimal::add);
        }
        return buildPlanFromPurchaseMap(payment, consolidated, applyToPurchases);
    }

    private AllocationPlan buildAutoPlan(SupplierPayment payment, boolean applyToPurchases) {
        Map<Long, BigDecimal> consolidated = new LinkedHashMap<>();
        BigDecimal remaining = safe(payment.getAmount());
        for (PurchaseOrder purchase : purchaseOrderRepository.findUnpaidBySupplierIdOrderByPurchaseDateAscIdAsc(payment.getSupplier().getId())) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal due = safe(purchase.getDueAmount());
            if (due.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal allocate = remaining.min(due);
            if (allocate.compareTo(BigDecimal.ZERO) > 0) {
                consolidated.put(purchase.getId(), allocate);
                remaining = remaining.subtract(allocate);
            }
        }
        return buildPlanFromPurchaseMap(payment, consolidated, applyToPurchases);
    }

    private AllocationPlan buildPlanFromPurchaseMap(SupplierPayment payment, Map<Long, BigDecimal> consolidated, boolean applyToPurchases) {
        List<SupplierPaymentAllocation> allocations = new ArrayList<>();
        BigDecimal totalAllocated = BigDecimal.ZERO;

        for (Map.Entry<Long, BigDecimal> entry : consolidated.entrySet()) {
            PurchaseOrder purchase = findPurchaseById(entry.getKey());
            if (purchase.getSupplier() == null || !purchase.getSupplier().getId().equals(payment.getSupplier().getId())) {
                throw new BadRequestException("Allocation purchase does not belong to selected supplier.");
            }
            BigDecimal purchaseDue = safe(purchase.getDueAmount());
            BigDecimal allocated = normalizedPositive(entry.getValue(), "Allocated amount must be greater than zero");
            if (allocated.compareTo(purchaseDue) > 0) {
                throw new BadRequestException("Allocation cannot exceed purchase due amount.");
            }
            totalAllocated = totalAllocated.add(allocated);
            allocations.add(buildAllocation(payment, purchase, allocated));
        }

        BigDecimal paymentAmount = safe(payment.getAmount());
        if (totalAllocated.compareTo(paymentAmount) > 0) {
            throw new BadRequestException("Allocation cannot exceed payment amount.");
        }

        BigDecimal unapplied = paymentAmount.subtract(totalAllocated).max(BigDecimal.ZERO);
        if (applyToPurchases) {
            applyAllocationsToPurchases(allocations);
        }
        return new AllocationPlan(allocations, totalAllocated, unapplied);
    }

    private SupplierPaymentAllocation buildAllocation(SupplierPayment payment, PurchaseOrder purchase, BigDecimal allocatedAmount) {
        SupplierPaymentAllocation allocation = new SupplierPaymentAllocation();
        allocation.setSupplierPayment(payment);
        allocation.setPurchaseOrder(purchase);
        allocation.setAllocatedAmount(allocatedAmount);
        return allocation;
    }

    private void applyAllocationPlan(SupplierPayment payment, AllocationPlan plan) {
        payment.getAllocations().clear();
        payment.getAllocations().addAll(plan.allocations());
        for (SupplierPaymentAllocation allocation : payment.getAllocations()) {
            allocation.setSupplierPayment(payment);
        }
    }

    private void applyAllocationsToPurchases(List<SupplierPaymentAllocation> allocations) {
        for (SupplierPaymentAllocation allocation : allocations) {
            PurchaseOrder purchase = allocation.getPurchaseOrder();
            BigDecimal allocated = safe(allocation.getAllocatedAmount());
            BigDecimal newPaid = safe(purchase.getPaidAmount()).add(allocated);
            BigDecimal newDue = safe(purchase.getDueAmount()).subtract(allocated).max(BigDecimal.ZERO);
            purchase.setPaidAmount(newPaid);
            purchase.setDueAmount(newDue);
            purchase.setStatus(resolvePurchaseStatus(newPaid, newDue));
            purchaseOrderRepository.save(purchase);
        }
    }

    private PurchaseStatus resolvePurchaseStatus(BigDecimal paidAmount, BigDecimal dueAmount) {
        if (dueAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return paidAmount.compareTo(BigDecimal.ZERO) > 0 ? PurchaseStatus.PAID : PurchaseStatus.RECEIVED;
        }
        if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            return PurchaseStatus.PARTIAL_PAID;
        }
        return PurchaseStatus.RECEIVED;
    }

    private void rollbackAllocations(SupplierPayment payment) {
        if (payment.getAllocations() == null) {
            return;
        }
        for (SupplierPaymentAllocation allocation : payment.getAllocations()) {
            PurchaseOrder purchase = allocation.getPurchaseOrder();
            if (purchase == null) {
                continue;
            }
            BigDecimal allocated = safe(allocation.getAllocatedAmount());
            BigDecimal newPaid = safe(purchase.getPaidAmount()).subtract(allocated).max(BigDecimal.ZERO);
            BigDecimal newDue = safe(purchase.getDueAmount()).add(allocated);
            purchase.setPaidAmount(newPaid);
            purchase.setDueAmount(newDue);
            purchase.setStatus(resolvePurchaseStatus(newPaid, newDue));
            purchaseOrderRepository.save(purchase);
        }
    }

    private JournalEntry buildJournalEntry(SupplierPayment payment) {
        Account creditAccount = payment.getPaymentMethod() == SupplierPaymentMethod.BANK
                || payment.getPaymentMethod() == SupplierPaymentMethod.MOBILE_BANKING
                || payment.getPaymentMethod() == SupplierPaymentMethod.CHEQUE
                ? account("Bank")
                : account("Cash");

        JournalEntry entry = new JournalEntry();
        entry.setJournalNo(nextJournalNo());
        entry.setJournalDate(payment.getPaymentDate());
        entry.setReferenceNo(payment.getPaymentNo());
        entry.setDescription("Supplier payment posting " + payment.getPaymentNo());
        entry.setSourceType(SOURCE_TYPE);
        entry.setSourceId(payment.getId());
        entry.setSourceNo(payment.getPaymentNo());
        entry.setStatus(JournalStatus.POSTED);

        BigDecimal allocated = safe(payment.getTotalAllocatedAmount());
        BigDecimal unapplied = safe(payment.getUnappliedAmount());
        if (allocated.signum() > 0) {
            addLine(entry, account("Accounts Payable"), allocated, BigDecimal.ZERO, "Payable reduction");
        }
        if (unapplied.signum() > 0) {
            addLine(entry, account("Supplier Advance"), unapplied, BigDecimal.ZERO, "Supplier advance");
        }
        addLine(entry, creditAccount, BigDecimal.ZERO, payment.getAmount(), "Supplier payment");
        return entry;
    }

    private JournalEntry buildReversalJournalEntry(SupplierPayment payment) {
        Account debitAccount = payment.getPaymentMethod() == SupplierPaymentMethod.BANK
                || payment.getPaymentMethod() == SupplierPaymentMethod.MOBILE_BANKING
                || payment.getPaymentMethod() == SupplierPaymentMethod.CHEQUE
                ? account("Bank")
                : account("Cash");

        JournalEntry entry = new JournalEntry();
        entry.setJournalNo(nextJournalNo());
        entry.setJournalDate(LocalDate.now());
        entry.setReferenceNo(payment.getPaymentNo());
        entry.setDescription("Supplier payment reversal " + payment.getPaymentNo());
        entry.setSourceType(REVERSAL_SOURCE_TYPE);
        entry.setSourceId(payment.getId());
        entry.setSourceNo(payment.getPaymentNo() + "-REV");
        entry.setStatus(JournalStatus.POSTED);

        addLine(entry, debitAccount, payment.getAmount(), BigDecimal.ZERO, "Supplier payment reversal");
        BigDecimal allocated = safe(payment.getTotalAllocatedAmount());
        BigDecimal unapplied = safe(payment.getUnappliedAmount());
        if (allocated.signum() > 0) {
            addLine(entry, account("Accounts Payable"), BigDecimal.ZERO, allocated, "Restore payable");
        }
        if (unapplied.signum() > 0) {
            addLine(entry, account("Supplier Advance"), BigDecimal.ZERO, unapplied, "Reverse supplier advance");
        }
        return entry;
    }

    private void addLine(JournalEntry entry, Account account, BigDecimal debit, BigDecimal credit, String description) {
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account);
        line.setDebit(safe(debit));
        line.setCredit(safe(credit));
        line.setDescription(description);
        entry.addLine(line);
    }

    private void saveIfBalanced(JournalEntry entry) {
        BigDecimal debit = entry.getLines().stream().map(JournalEntryLine::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credit = entry.getLines().stream().map(JournalEntryLine::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (debit.signum() <= 0 || debit.compareTo(credit) != 0) {
            throw new BadRequestException("Supplier payment journal entry is not balanced.");
        }
        journalEntryRepository.save(entry);
    }

    private void validateCreate(SupplierPaymentDTO dto) {
        if (dto.getSupplierId() == null) {
            throw new BadRequestException("Supplier is required");
        }
        if (dto.getPaymentDate() == null) {
            throw new BadRequestException("Payment date is required");
        }
        if (dto.getPaymentMethod() == null) {
            throw new BadRequestException("Payment method is required");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
    }

    private void ensureEditable(SupplierPayment payment) {
        if (payment.getStatus() == SupplierPaymentStatus.POSTED) {
            throw new BadRequestException("Posted supplier payment cannot be edited.");
        }
        if (payment.getStatus() == SupplierPaymentStatus.CANCELLED) {
            throw new BadRequestException("Cancelled supplier payment cannot be edited.");
        }
    }

    private void normalize(SupplierPaymentDTO dto) {
        dto.setReferenceNo(RequestValueUtils.normalize(dto.getReferenceNo()));
        dto.setNotes(RequestValueUtils.normalize(dto.getNotes()));
        if (dto.getAllocationMode() == null) {
            dto.setAllocationMode(SupplierPaymentAllocationMode.AUTO);
        }
        if (dto.getAllocations() == null) {
            dto.setAllocations(new ArrayList<>());
        }
    }

    private SupplierPayment findPaymentByIdDetailed(Long id) {
        return paymentRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier payment not found with id: " + id));
    }

    private Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    private PurchaseOrder findPurchaseById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }

    private Account account(String name) {
        return accountRepository.findByAccountNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Required account not found: " + name));
    }

    private String resolvePaymentNo() {
        long nextNumber = paymentRepository.findTopByOrderByIdDesc()
                .map(payment -> payment.getId() + 1)
                .orElse(1L);
        String generated = String.format("SP-%06d", nextNumber);
        while (paymentRepository.existsByPaymentNo(generated)) {
            nextNumber++;
            generated = String.format("SP-%06d", nextNumber);
        }
        return generated;
    }

    private Sort sortFor(String sort, String direction) {
        String property = switch (sort == null ? "" : sort) {
            case "paymentNo" -> "paymentNo";
            case "supplierName" -> "supplier.name";
            case "supplierCode" -> "supplier.supplierCode";
            case "paymentDate" -> "paymentDate";
            case "amount" -> "amount";
            case "paymentMethod" -> "paymentMethod";
            case "status" -> "status";
            case "postedAt" -> "postedAt";
            default -> "paymentDate";
        };
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, property).and(Sort.by(Sort.Direction.DESC, "id"));
    }

    private int safeSize(int size) {
        return size == 25 || size == 50 || size == 100 ? size : 10;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal normalizedPositive(BigDecimal value, String message) {
        BigDecimal normalized = safe(value);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(message);
        }
        return normalized;
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

    private record AllocationPlan(List<SupplierPaymentAllocation> allocations, BigDecimal totalAllocatedAmount, BigDecimal unappliedAmount) {
    }
}
