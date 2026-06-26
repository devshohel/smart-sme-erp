package com.sme.erp.supplier.service.impl;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryLineRepository;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.enums.Status;
import com.sme.erp.notification.enums.NotificationSeverity;
import com.sme.erp.notification.enums.NotificationType;
import com.sme.erp.notification.service.NotificationService;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.entity.PurchaseReturn;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.repository.PurchaseReturnRepository;
import com.sme.erp.supplier.dto.ApReconciliationBreakdownDTO;
import com.sme.erp.supplier.dto.ApReconciliationDTO;
import com.sme.erp.supplier.dto.ApReconciliationRowDTO;
import com.sme.erp.supplier.dto.ApReconciliationSummaryDTO;
import com.sme.erp.supplier.dto.SupplierAgingReportDTO;
import com.sme.erp.supplier.dto.SupplierAgingRowDTO;
import com.sme.erp.supplier.dto.SupplierDetailDTO;
import com.sme.erp.supplier.dto.SupplierDTO;
import com.sme.erp.supplier.dto.SupplierLedgerDTO;
import com.sme.erp.supplier.dto.SupplierLedgerEntryDTO;
import com.sme.erp.supplier.dto.SupplierOptionDTO;
import com.sme.erp.supplier.dto.SupplierPageDTO;
import com.sme.erp.supplier.dto.SupplierStatementDTO;
import com.sme.erp.supplier.dto.SupplierStatementRowDTO;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.mapper.SupplierMapper;
import com.sme.erp.supplier.payment.entity.SupplierPayment;
import com.sme.erp.supplier.payment.enums.SupplierPaymentStatus;
import com.sme.erp.supplier.payment.mapper.SupplierPaymentMapper;
import com.sme.erp.supplier.payment.repository.SupplierPaymentRepository;
import com.sme.erp.supplier.repository.SupplierRepository;
import com.sme.erp.supplier.service.SupplierService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseReturnRepository purchaseReturnRepository;
    private final SupplierPaymentRepository supplierPaymentRepository;
    private final SupplierPaymentMapper supplierPaymentMapper;
    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public SupplierServiceImpl(
            SupplierRepository supplierRepository,
            SupplierMapper supplierMapper,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseReturnRepository purchaseReturnRepository,
            SupplierPaymentRepository supplierPaymentRepository,
            SupplierPaymentMapper supplierPaymentMapper,
            AccountRepository accountRepository,
            JournalEntryLineRepository journalEntryLineRepository,
            ActivityLogService activityLogService,
            AuditLogService auditLogService,
            NotificationService notificationService) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.supplierPaymentRepository = supplierPaymentRepository;
        this.supplierPaymentMapper = supplierPaymentMapper;
        this.accountRepository = accountRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> getAll(String keyword, Status status) {
        String normalizedKeyword = RequestValueUtils.normalize(keyword);
        return supplierRepository.search(normalizedKeyword, status)
                .stream()
                .map(this::toDtoWithDue)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> getDeleted() {
        return supplierRepository.findDeletedSuppliers()
                .stream()
                .map(this::toDtoWithDue)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierPageDTO searchPage(String keyword, Status status, int page, int size, String sort, String direction) {
        String normalizedKeyword = RequestValueUtils.normalize(keyword);
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                buildSort(sort, direction));
        Page<Supplier> supplierPage = supplierRepository.searchPage(normalizedKeyword, status, pageable);
        List<SupplierDTO> content = supplierPage.getContent().stream()
                .map(this::toDtoWithDue)
                .collect(Collectors.toList());
        return new SupplierPageDTO(content, supplierPage.getTotalElements(), supplierPage.getTotalPages(),
                supplierPage.getNumber(), supplierPage.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierOptionDTO> autocomplete(String keyword) {
        String normalizedKeyword = RequestValueUtils.normalize(keyword);
        return supplierRepository.autocomplete(normalizedKeyword, PageRequest.of(0, 20));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO getById(Long id) {
        return toDtoWithDue(findSupplierById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDetailDTO getDetail(Long id) {
        Supplier supplier = findSupplierById(id);
        SupplierDTO supplierDTO = toDtoWithDue(supplier);
        BigDecimal supplierDue = supplierDTO.getSupplierDue() != null ? supplierDTO.getSupplierDue() : BigDecimal.ZERO;
        return new SupplierDetailDTO(
                supplierDTO,
                supplierDue,
                purchaseOrderRepository.findRecentPurchaseSummariesBySupplierId(id, PageRequest.of(0, 10)),
                purchaseReturnRepository.findRecentReturnSummariesBySupplierId(id, PageRequest.of(0, 10)),
                supplierPaymentRepository.findBySupplierIdOrderByPaymentDateDescIdDesc(id, PageRequest.of(0, 10))
                        .stream()
                        .map(supplierPaymentMapper::toDTO)
                        .collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierLedgerDTO getLedger(Long id, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("From date cannot be after to date.");
        }

        Supplier supplier = findSupplierById(id);
        BigDecimal openingBalance = safe(supplier.getOpeningBalance());
        if (fromDate != null) {
            openingBalance = applyEntries(openingBalance, ledgerEntries(supplier.getId(), null, fromDate.minusDays(1)));
        }

        List<LedgerEntry> sourceEntries = ledgerEntries(supplier.getId(), fromDate, toDate);
        BigDecimal runningBalance = openingBalance;
        List<SupplierLedgerEntryDTO> entries = new ArrayList<>();
        entries.add(new SupplierLedgerEntryDTO(
                fromDate != null ? fromDate : createdDate(supplier),
                "OPENING_BALANCE",
                supplier.getSupplierCode(),
                fromDate != null ? "Opening balance before selected period" : "Supplier opening balance",
                BigDecimal.ZERO,
                openingBalance,
                openingBalance));

        for (LedgerEntry source : sourceEntries) {
            runningBalance = runningBalance.add(source.credit()).subtract(source.debit());
            entries.add(new SupplierLedgerEntryDTO(
                    source.date(),
                    source.referenceType(),
                    source.referenceNo(),
                    source.description(),
                    source.debit(),
                    source.credit(),
                    runningBalance));
        }

        return new SupplierLedgerDTO(
                toDtoWithDue(supplier),
                fromDate,
                toDate,
                openingBalance,
                runningBalance,
                entries);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierStatementDTO getStatement(Long id, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("From date cannot be after to date.");
        }

        Supplier supplier = findSupplierById(id);
        List<StatementEntry> allEntries = statementEntries(supplier.getId());
        BigDecimal openingBalance = safe(supplier.getOpeningBalance());
        if (fromDate != null) {
            openingBalance = applyStatementEntries(openingBalance, allEntries.stream()
                    .filter(entry -> entry.date().isBefore(fromDate))
                    .collect(Collectors.toList()));
        }

        BigDecimal runningBalance = openingBalance;
        List<SupplierStatementRowDTO> rows = new ArrayList<>();
        rows.add(new SupplierStatementRowDTO(
                fromDate != null ? fromDate : createdDate(supplier),
                "OPENING_BALANCE",
                supplier.getSupplierCode(),
                fromDate != null ? "Opening payable balance before selected period" : "Supplier opening payable balance",
                BigDecimal.ZERO,
                openingBalance,
                openingBalance,
                BigDecimal.ZERO,
                "OPEN"));

        for (StatementEntry entry : allEntries) {
            if (!withinPeriod(entry.date(), fromDate, toDate)) {
                continue;
            }
            runningBalance = runningBalance.add(entry.credit()).subtract(entry.debit());
            rows.add(new SupplierStatementRowDTO(
                    entry.date(),
                    entry.referenceType(),
                    entry.referenceNo(),
                    entry.description(),
                    entry.debit(),
                    entry.credit(),
                    runningBalance,
                    entry.advanceAmount(),
                    entry.status()));
        }

        BigDecimal supplierAdvanceBalance = supplierAdvanceBalance(allEntries, toDate);
        BigDecimal netSupplierPosition = runningBalance.subtract(supplierAdvanceBalance);
        return new SupplierStatementDTO(
                supplierMapper.toDTO(supplier),
                fromDate,
                toDate,
                openingBalance,
                runningBalance,
                supplierAdvanceBalance,
                netSupplierPosition,
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierAgingReportDTO getAging(Long supplierId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("From date cannot be after to date.");
        }

        LocalDateTime fromDateTime = startOfDay(fromDate);
        LocalDateTime toDateTime = exclusiveEnd(toDate);
        LocalDate asOfDate = toDate != null ? toDate : LocalDate.now();
        Map<Long, AgingAccumulator> rows = new LinkedHashMap<>();

        for (PurchaseOrder purchase : purchaseOrderRepository.findDuePurchasesForAging(supplierId, fromDateTime, toDateTime)) {
            Supplier supplier = purchase.getSupplier();
            if (supplier == null || supplier.getId() == null) {
                continue;
            }
            AgingAccumulator accumulator = rows.computeIfAbsent(supplier.getId(), ignored ->
                    new AgingAccumulator(supplier.getId(), supplier.getSupplierCode(), supplier.getName()));
            accumulator.add(safe(purchase.getDueAmount()), daysBetween(purchase.getPurchaseDate(), asOfDate));
        }

        List<SupplierAgingRowDTO> resultRows = rows.values().stream()
                .map(AgingAccumulator::toDTO)
                .collect(Collectors.toList());
        BigDecimal totalDue = resultRows.stream()
                .map(SupplierAgingRowDTO::getTotalDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SupplierAgingReportDTO(fromDate, toDate, totalDue, resultRows);
    }

    @Override
    @Transactional(readOnly = true)
    public ApReconciliationDTO getApReconciliation(Long supplierId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("From date cannot be after to date.");
        }

        Map<Long, ApRowAccumulator> rows = new LinkedHashMap<>();
        if (supplierId != null) {
            Supplier supplier = findSupplierById(supplierId);
            rows.put(supplier.getId(), new ApRowAccumulator(supplier.getId(), supplier.getSupplierCode(), supplier.getName()));
        } else {
            supplierRepository.search(null, null).forEach(supplier ->
                    rows.put(supplier.getId(), new ApRowAccumulator(supplier.getId(), supplier.getSupplierCode(), supplier.getName())));
        }

        LocalDateTime fromDateTime = startOfDay(fromDate);
        LocalDateTime toDateTime = exclusiveEnd(toDate);
        BigDecimal purchaseGross = BigDecimal.ZERO;
        BigDecimal purchaseReturns = BigDecimal.ZERO;
        BigDecimal allocatedPayments = BigDecimal.ZERO;
        BigDecimal paymentReversals = BigDecimal.ZERO;
        BigDecimal manualApAdjustments = BigDecimal.ZERO;

        for (Object[] row : purchaseOrderRepository.findApReconciliationPurchaseRows(supplierId, fromDateTime, toDateTime)) {
            ApRowAccumulator accumulator = row(rows, id(row[0]));
            accumulator.purchaseDue = accumulator.purchaseDue.add(amount(row[1]));
        }

        for (Object[] row : supplierPaymentRepository.findApReconciliationAdvanceRows(supplierId, fromDate, toDate)) {
            ApRowAccumulator accumulator = row(rows, id(row[0]));
            accumulator.supplierAdvance = accumulator.supplierAdvance.add(amount(row[1]));
        }

        Account apAccount = accountRepository.findByAccountNameIgnoreCase("Accounts Payable").orElse(null);
        if (apAccount != null) {
            for (JournalEntryLine line : journalEntryLineRepository.findPostedLedgerLines(apAccount.getId(), fromDate, toDate)) {
                String sourceType = line.getJournalEntry().getSourceType();
                BigDecimal signedAp = safe(line.getCredit()).subtract(safe(line.getDebit()));
                Long sourceSupplierId = supplierIdForApSource(sourceType, line.getJournalEntry().getSourceId());
                if (sourceSupplierId == null || (supplierId != null && !supplierId.equals(sourceSupplierId))) {
                    if (supplierId == null) {
                        manualApAdjustments = manualApAdjustments.add(signedAp);
                    }
                    continue;
                }

                ApRowAccumulator accumulator = row(rows, sourceSupplierId);
                accumulator.glAccountsPayable = accumulator.glAccountsPayable.add(signedAp);
                if ("PURCHASE".equals(sourceType)) {
                    purchaseGross = purchaseGross.add(signedAp.max(BigDecimal.ZERO));
                } else if ("PURCHASE_RETURN".equals(sourceType)) {
                    purchaseReturns = purchaseReturns.add(signedAp.negate().max(BigDecimal.ZERO));
                } else if ("SUPPLIER_PAYMENT".equals(sourceType)) {
                    allocatedPayments = allocatedPayments.add(signedAp.negate().max(BigDecimal.ZERO));
                } else if ("SUPPLIER_PAYMENT_REVERSAL".equals(sourceType)) {
                    paymentReversals = paymentReversals.add(signedAp.max(BigDecimal.ZERO));
                }
            }
        }

        BigDecimal totalPurchaseDue = BigDecimal.ZERO;
        BigDecimal totalSupplierAdvance = BigDecimal.ZERO;
        BigDecimal totalGlAccountsPayable = BigDecimal.ZERO;
        List<ApReconciliationRowDTO> resultRows = new ArrayList<>();
        for (ApRowAccumulator row : rows.values()) {
            if (!row.hasValues() && supplierId == null) {
                continue;
            }
            totalPurchaseDue = totalPurchaseDue.add(row.purchaseDue);
            totalSupplierAdvance = totalSupplierAdvance.add(row.supplierAdvance);
            totalGlAccountsPayable = totalGlAccountsPayable.add(row.glAccountsPayable);
            resultRows.add(row.toDTO(false));
        }

        if (supplierId == null && manualApAdjustments.compareTo(BigDecimal.ZERO) != 0) {
            totalGlAccountsPayable = totalGlAccountsPayable.add(manualApAdjustments);
            resultRows.add(new ApReconciliationRowDTO(
                    null,
                    "MANUAL",
                    "Manual / Unknown AP Adjustments",
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    manualApAdjustments,
                    manualApAdjustments,
                    BigDecimal.ZERO,
                    "REVIEW_NEEDED"));
        }

        resultRows.sort(Comparator.comparing(ApReconciliationRowDTO::getSupplierName));
        BigDecimal totalVariance = totalGlAccountsPayable.subtract(totalPurchaseDue);
        BigDecimal netSupplierExposure = totalPurchaseDue.subtract(totalSupplierAdvance);
        return new ApReconciliationDTO(
                new ApReconciliationSummaryDTO(totalPurchaseDue, totalSupplierAdvance, totalGlAccountsPayable, totalVariance, netSupplierExposure),
                resultRows,
                new ApReconciliationBreakdownDTO(purchaseGross, purchaseReturns, allocatedPayments, paymentReversals,
                        totalSupplierAdvance, manualApAdjustments));
    }

    @Override
    @Transactional
    public SupplierDTO create(SupplierDTO dto) {
        normalizeDto(dto);
        validateFinancials(dto);

        Supplier supplier = new Supplier();
        supplier.setSupplierCode(resolveSupplierCodeForCreate(dto.getSupplierCode()));
        validateEmailUnique(dto.getEmail(), null);
        validatePhoneUnique(dto.getPhone(), null);

        supplierMapper.updateEntity(dto, supplier);
        supplier.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        supplier.setOpeningBalance(safeNonNegative(dto.getOpeningBalance(), "Opening balance cannot be negative"));
        supplier.setCurrentBalance(supplier.getOpeningBalance());
        supplier.setIsDeleted(false);

        // Supplier balance movements remain isolated from accounting ledger postings in this phase.

        SupplierDTO saved = supplierMapper.toDTO(supplierRepository.save(supplier));
        activityLogService.log("SUPPLIER_CREATE", "SUPPLIER", "suppliers", saved.getId(), "Created supplier " + saved.getName());
        auditLogService.log("suppliers", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        notificationService.notifyGlobal(
                "New supplier created",
                "Supplier " + saved.getName() + " was created.",
                NotificationType.SUCCESS,
                NotificationSeverity.LOW,
                "SUPPLIER",
                saved.getId(),
                "/suppliers/details/" + saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public SupplierDTO update(Long id, SupplierDTO dto) {
        normalizeDto(dto);
        validateFinancials(dto);

        Supplier supplier = findSupplierById(id);
        SupplierDTO oldData = supplierMapper.toDTO(supplier);
        String requestedSupplierCode = dto.getSupplierCode();
        if (requestedSupplierCode != null) {
            validateSupplierCodeUnique(requestedSupplierCode, id);
            supplier.setSupplierCode(requestedSupplierCode);
        }
        validateEmailUnique(dto.getEmail(), id);
        validatePhoneUnique(dto.getPhone(), id);

        BigDecimal existingOpeningBalance = supplier.getOpeningBalance() != null
                ? supplier.getOpeningBalance()
                : BigDecimal.ZERO;
        supplierMapper.updateEntity(dto, supplier);
        supplier.setOpeningBalance(dto.getOpeningBalance() != null
                ? safeNonNegative(dto.getOpeningBalance(), "Opening balance cannot be negative")
                : existingOpeningBalance);
        supplier.setCurrentBalance(oldData.getCurrentBalance() != null ? oldData.getCurrentBalance() : BigDecimal.ZERO);
        if (supplier.getStatus() == null) {
            supplier.setStatus(Status.ACTIVE);
        }

        SupplierDTO saved = toDtoWithDue(supplierRepository.save(supplier));
        auditLogService.log("suppliers", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Supplier supplier = findSupplierById(id);
        SupplierDTO oldData = supplierMapper.toDTO(supplier);
        supplierRepository.delete(supplier);
        auditLogService.log("suppliers", id, auditLogService.toJson(oldData), null, "DELETE");
    }

    @Override
    @Transactional
    public SupplierDTO restore(Long id) {
        int updated = supplierRepository.restoreById(id);
        if (updated == 0) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }
        SupplierDTO restored = toDtoWithDue(findSupplierById(id));
        activityLogService.log("SUPPLIER_RESTORE", "SUPPLIER", "suppliers", id, "Restored supplier " + restored.getName());
        auditLogService.log("suppliers", id, null, auditLogService.toJson(restored), "RESTORE");
        return restored;
    }

    private void normalizeDto(SupplierDTO dto) {
        dto.setSupplierCode(RequestValueUtils.normalize(dto.getSupplierCode()));
        dto.setName(RequestValueUtils.normalizeRequired(dto.getName(), "Supplier name"));
        dto.normalizeCompanyName(RequestValueUtils.normalize(dto.getCompanyName()));
        dto.normalizeContactPerson(RequestValueUtils.normalize(dto.getContactPerson()));
        dto.normalizePhone(RequestValueUtils.normalize(dto.getPhone()));
        dto.normalizeEmail(RequestValueUtils.normalize(dto.getEmail()));
        dto.normalizeAddress(RequestValueUtils.normalize(dto.getAddress()));
        dto.normalizeCity(RequestValueUtils.normalize(dto.getCity()));
        dto.normalizeCountry(RequestValueUtils.normalize(dto.getCountry()));
        dto.normalizePostalCode(RequestValueUtils.normalize(dto.getPostalCode()));
        dto.normalizeTaxNumber(RequestValueUtils.normalize(dto.getTaxNumber()));
        dto.normalizeBankAccount(RequestValueUtils.normalize(dto.getBankAccount()));
        dto.normalizePaymentTerms(RequestValueUtils.normalize(dto.getPaymentTerms()));
    }

    private void validateFinancials(SupplierDTO dto) {
        safeNonNegative(dto.getOpeningBalance(), "Opening balance cannot be negative");
    }

    private BigDecimal safeNonNegative(BigDecimal value, String message) {
        BigDecimal normalized = value != null ? value : BigDecimal.ZERO;
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(message);
        }
        return normalized;
    }

    private String resolveSupplierCodeForCreate(String requestedCode) {
        if (requestedCode != null) {
            validateSupplierCodeUnique(requestedCode, null);
            return requestedCode;
        }

        long nextNumber = supplierRepository.findMaxIdIncludingDeleted() + 1;
        String generated = String.format("SUP-%04d", nextNumber);
        while (existsBySupplierCodeIncludingDeleted(generated)) {
            nextNumber++;
            generated = String.format("SUP-%04d", nextNumber);
        }
        return generated;
    }

    private void validateSupplierCodeUnique(String supplierCode, Long currentId) {
        boolean exists = currentId == null
                ? existsBySupplierCodeIncludingDeleted(supplierCode)
                : existsBySupplierCodeAndIdNotIncludingDeleted(supplierCode, currentId);
        if (exists) {
            throw new DuplicateResourceException("Supplier code already exists: " + supplierCode);
        }
    }

    private boolean existsBySupplierCodeIncludingDeleted(String supplierCode) {
        return hasRows(supplierRepository.countBySupplierCodeIncludingDeleted(supplierCode));
    }

    private boolean existsBySupplierCodeAndIdNotIncludingDeleted(String supplierCode, Long currentId) {
        return hasRows(supplierRepository.countBySupplierCodeAndIdNotIncludingDeleted(supplierCode, currentId));
    }

    private boolean hasRows(Long count) {
        return count != null && count > 0;
    }

    private void validateEmailUnique(String email, Long currentId) {
        if (email == null) {
            return;
        }
        boolean exists = currentId == null
                ? supplierRepository.existsByEmail(email)
                : supplierRepository.existsByEmailAndIdNot(email, currentId);
        if (exists) {
            throw new DuplicateResourceException("Supplier email already exists: " + email);
        }
    }

    private void validatePhoneUnique(String phone, Long currentId) {
        if (phone == null) {
            return;
        }
        boolean exists = currentId == null
                ? supplierRepository.existsByPhone(phone)
                : supplierRepository.existsByPhoneAndIdNot(phone, currentId);
        if (exists) {
            throw new DuplicateResourceException("Supplier phone already exists: " + phone);
        }
    }

    private Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    private SupplierDTO toDtoWithDue(Supplier supplier) {
        SupplierDTO dto = supplierMapper.toDTO(supplier);
        dto.setSupplierDue(calculateSupplierDue(supplier.getId()));
        return dto;
    }

    private BigDecimal calculateSupplierDue(Long supplierId) {
        BigDecimal due = purchaseOrderRepository.calculateSupplierDue(supplierId);
        return due != null ? due : BigDecimal.ZERO;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Long supplierIdForApSource(String sourceType, Long sourceId) {
        if (sourceType == null || sourceId == null) {
            return null;
        }
        if ("PURCHASE".equals(sourceType)) {
            return purchaseOrderRepository.findById(sourceId)
                    .map(PurchaseOrder::getSupplier)
                    .map(Supplier::getId)
                    .orElse(null);
        }
        if ("PURCHASE_RETURN".equals(sourceType)) {
            return purchaseReturnRepository.findById(sourceId)
                    .map(PurchaseReturn::getSupplier)
                    .map(Supplier::getId)
                    .orElse(null);
        }
        if ("SUPPLIER_PAYMENT".equals(sourceType) || "SUPPLIER_PAYMENT_REVERSAL".equals(sourceType)) {
            return supplierPaymentRepository.findById(sourceId)
                    .map(SupplierPayment::getSupplier)
                    .map(Supplier::getId)
                    .orElse(null);
        }
        return null;
    }

    private ApRowAccumulator row(Map<Long, ApRowAccumulator> rows, Long supplierId) {
        return rows.computeIfAbsent(supplierId, id -> {
            Supplier supplier = findSupplierById(id);
            return new ApRowAccumulator(supplier.getId(), supplier.getSupplierCode(), supplier.getName());
        });
    }

    private Long id(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private BigDecimal amount(Object value) {
        return value instanceof BigDecimal ? (BigDecimal) value : BigDecimal.ZERO;
    }

    private List<LedgerEntry> ledgerEntries(Long supplierId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = startOfDay(fromDate);
        LocalDateTime toDateTime = exclusiveEnd(toDate);
        List<LedgerEntry> entries = new ArrayList<>();

        for (PurchaseOrder purchase : purchaseOrderRepository.findPostedBySupplierForLedger(supplierId, fromDateTime, toDateTime)) {
            entries.add(new LedgerEntry(
                    purchase.getPurchaseDate().toLocalDate(),
                    1,
                    purchase.getId(),
                    "PURCHASE",
                    purchase.getPurchaseCode(),
                    "Purchase payable",
                    BigDecimal.ZERO,
                    safe(purchase.getNetTotal())));
        }

        for (SupplierPayment payment : supplierPaymentRepository.findPostedBySupplierForLedger(supplierId, fromDate, toDate)) {
            entries.add(new LedgerEntry(
                    payment.getPaymentDate(),
                    2,
                    payment.getId(),
                    "SUPPLIER_PAYMENT",
                    payment.getPaymentNo(),
                    "Supplier payment",
                    safe(payment.getAmount()),
                    BigDecimal.ZERO));
        }

        for (PurchaseReturn purchaseReturn : purchaseReturnRepository.findBySupplierForLedger(supplierId, fromDateTime, toDateTime)) {
            entries.add(new LedgerEntry(
                    purchaseReturn.getReturnDate().toLocalDate(),
                    3,
                    purchaseReturn.getId(),
                    "PURCHASE_RETURN",
                    purchaseReturn.getReturnCode(),
                    "Purchase return",
                    safe(purchaseReturn.getTotalAmount()),
                    BigDecimal.ZERO));
        }

        entries.sort(Comparator
                .comparing(LedgerEntry::date)
                .thenComparing(LedgerEntry::sortOrder)
                .thenComparing(LedgerEntry::id));
        return entries;
    }

    private List<StatementEntry> statementEntries(Long supplierId) {
        List<StatementEntry> entries = new ArrayList<>();

        for (PurchaseOrder purchase : purchaseOrderRepository.findPostedBySupplierForLedger(supplierId, null, null)) {
            entries.add(new StatementEntry(
                    purchase.getPurchaseDate().toLocalDate(),
                    1,
                    purchase.getId(),
                    "PURCHASE",
                    purchase.getPurchaseCode(),
                    "Purchase payable",
                    BigDecimal.ZERO,
                    safe(purchase.getNetTotal()),
                    BigDecimal.ZERO,
                    purchase.getStatus() != null ? purchase.getStatus().name() : ""));
        }

        for (PurchaseReturn purchaseReturn : purchaseReturnRepository.findBySupplierForLedger(supplierId, null, null)) {
            entries.add(new StatementEntry(
                    purchaseReturn.getReturnDate().toLocalDate(),
                    2,
                    purchaseReturn.getId(),
                    "PURCHASE_RETURN",
                    purchaseReturn.getReturnCode(),
                    "Purchase return reduces payable",
                    safe(purchaseReturn.getTotalAmount()),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "RETURNED"));
        }

        for (SupplierPayment payment : supplierPaymentRepository.findStatementPaymentsBySupplier(supplierId)) {
            BigDecimal allocated = safe(payment.getTotalAllocatedAmount());
            BigDecimal unapplied = safe(payment.getUnappliedAmount());
            if (allocated.signum() > 0 || unapplied.signum() > 0) {
                entries.add(new StatementEntry(
                        payment.getPaymentDate(),
                        3,
                        payment.getId(),
                        "SUPPLIER_PAYMENT",
                        payment.getPaymentNo(),
                        paymentDescription(payment, allocated, unapplied),
                        allocated,
                        BigDecimal.ZERO,
                        unapplied,
                        payment.getStatus() != null ? payment.getStatus().name() : ""));
            }

            if (payment.getStatus() == SupplierPaymentStatus.REVERSED) {
                entries.add(new StatementEntry(
                        reversalDate(payment),
                        4,
                        payment.getId(),
                        "SUPPLIER_PAYMENT_REVERSAL",
                        payment.getPaymentNo() + "-REV",
                        "Payment reversal restores payable and reverses supplier advance",
                        BigDecimal.ZERO,
                        allocated,
                        unapplied.negate(),
                        "REVERSED"));
            }
        }

        entries.sort(Comparator
                .comparing(StatementEntry::date)
                .thenComparing(StatementEntry::sortOrder)
                .thenComparing(StatementEntry::id));
        return entries;
    }

    private String paymentDescription(SupplierPayment payment, BigDecimal allocated, BigDecimal unapplied) {
        if (allocated.signum() > 0 && unapplied.signum() > 0) {
            return "Supplier payment allocated to purchases; unapplied amount held as supplier advance";
        }
        if (allocated.signum() > 0) {
            return "Supplier payment allocated to purchases";
        }
        return "Unapplied supplier advance";
    }

    private LocalDate reversalDate(SupplierPayment payment) {
        return payment.getReversedAt() != null ? payment.getReversedAt().toLocalDate() : payment.getPaymentDate();
    }

    private boolean withinPeriod(LocalDate date, LocalDate fromDate, LocalDate toDate) {
        return (fromDate == null || !date.isBefore(fromDate))
                && (toDate == null || !date.isAfter(toDate));
    }

    private BigDecimal applyStatementEntries(BigDecimal balance, List<StatementEntry> entries) {
        BigDecimal running = balance;
        for (StatementEntry entry : entries) {
            running = running.add(entry.credit()).subtract(entry.debit());
        }
        return running;
    }

    private BigDecimal supplierAdvanceBalance(List<StatementEntry> entries, LocalDate toDate) {
        BigDecimal balance = BigDecimal.ZERO;
        for (StatementEntry entry : entries) {
            if (toDate == null || !entry.date().isAfter(toDate)) {
                balance = balance.add(entry.advanceAmount());
            }
        }
        return balance;
    }

    private BigDecimal applyEntries(BigDecimal balance, List<LedgerEntry> entries) {
        BigDecimal running = balance;
        for (LedgerEntry entry : entries) {
            running = running.add(entry.credit()).subtract(entry.debit());
        }
        return running;
    }

    private LocalDate createdDate(Supplier supplier) {
        return supplier.getCreatedAt() != null ? supplier.getCreatedAt().toLocalDate() : LocalDate.now();
    }

    private LocalDateTime startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime exclusiveEnd(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay();
    }

    private long daysBetween(LocalDateTime sourceDate, LocalDate asOfDate) {
        if (sourceDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(sourceDate.toLocalDate(), asOfDate);
    }

    private Sort buildSort(String sort, String direction) {
        Set<String> allowedSorts = Set.of("id", "supplierCode", "name", "companyName", "city", "status", "createdAt", "currentBalance");
        String sortField = allowedSorts.contains(sort) ? sort : "createdAt";
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, sortField);
    }

    private record LedgerEntry(LocalDate date, int sortOrder, Long id, String referenceType, String referenceNo,
                               String description, BigDecimal debit, BigDecimal credit) {
    }

    private record StatementEntry(LocalDate date, int sortOrder, Long id, String referenceType, String referenceNo,
                                  String description, BigDecimal debit, BigDecimal credit,
                                  BigDecimal advanceAmount, String status) {
    }

    private static class ApRowAccumulator {
        private final Long supplierId;
        private final String supplierCode;
        private final String supplierName;
        private BigDecimal purchaseDue = BigDecimal.ZERO;
        private BigDecimal supplierAdvance = BigDecimal.ZERO;
        private BigDecimal glAccountsPayable = BigDecimal.ZERO;

        ApRowAccumulator(Long supplierId, String supplierCode, String supplierName) {
            this.supplierId = supplierId;
            this.supplierCode = supplierCode;
            this.supplierName = supplierName;
        }

        boolean hasValues() {
            return purchaseDue.compareTo(BigDecimal.ZERO) != 0
                    || supplierAdvance.compareTo(BigDecimal.ZERO) != 0
                    || glAccountsPayable.compareTo(BigDecimal.ZERO) != 0;
        }

        ApReconciliationRowDTO toDTO(boolean reviewNeeded) {
            BigDecimal variance = glAccountsPayable.subtract(purchaseDue);
            return new ApReconciliationRowDTO(
                    supplierId,
                    supplierCode,
                    supplierName,
                    purchaseDue,
                    supplierAdvance,
                    glAccountsPayable,
                    variance,
                    purchaseDue.subtract(supplierAdvance),
                    reviewNeeded ? "REVIEW_NEEDED" : variance.compareTo(BigDecimal.ZERO) == 0 ? "MATCHED" : "VARIANCE");
        }
    }

    private static class AgingAccumulator {
        private final Long supplierId;
        private final String supplierCode;
        private final String supplierName;
        private BigDecimal current = BigDecimal.ZERO;
        private BigDecimal days1To30 = BigDecimal.ZERO;
        private BigDecimal days31To60 = BigDecimal.ZERO;
        private BigDecimal days61To90 = BigDecimal.ZERO;
        private BigDecimal days90Plus = BigDecimal.ZERO;

        AgingAccumulator(Long supplierId, String supplierCode, String supplierName) {
            this.supplierId = supplierId;
            this.supplierCode = supplierCode;
            this.supplierName = supplierName;
        }

        void add(BigDecimal amount, long ageDays) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            if (ageDays <= 0) {
                current = current.add(amount);
            } else if (ageDays <= 30) {
                days1To30 = days1To30.add(amount);
            } else if (ageDays <= 60) {
                days31To60 = days31To60.add(amount);
            } else if (ageDays <= 90) {
                days61To90 = days61To90.add(amount);
            } else {
                days90Plus = days90Plus.add(amount);
            }
        }

        SupplierAgingRowDTO toDTO() {
            BigDecimal total = current.add(days1To30).add(days31To60).add(days61To90).add(days90Plus);
            return new SupplierAgingRowDTO(supplierId, supplierCode, supplierName, current, days1To30, days31To60, days61To90, days90Plus, total);
        }
    }
}
