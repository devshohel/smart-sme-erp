package com.sme.erp.supplier.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.enums.Status;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.entity.PurchaseReturn;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.repository.PurchaseReturnRepository;
import com.sme.erp.supplier.dto.SupplierAgingReportDTO;
import com.sme.erp.supplier.dto.SupplierAgingRowDTO;
import com.sme.erp.supplier.dto.SupplierDetailDTO;
import com.sme.erp.supplier.dto.SupplierDTO;
import com.sme.erp.supplier.dto.SupplierLedgerDTO;
import com.sme.erp.supplier.dto.SupplierLedgerEntryDTO;
import com.sme.erp.supplier.dto.SupplierOptionDTO;
import com.sme.erp.supplier.dto.SupplierPageDTO;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.mapper.SupplierMapper;
import com.sme.erp.supplier.payment.entity.SupplierPayment;
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
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;

    public SupplierServiceImpl(
            SupplierRepository supplierRepository,
            SupplierMapper supplierMapper,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseReturnRepository purchaseReturnRepository,
            SupplierPaymentRepository supplierPaymentRepository,
            SupplierPaymentMapper supplierPaymentMapper,
            ActivityLogService activityLogService,
            AuditLogService auditLogService) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.supplierPaymentRepository = supplierPaymentRepository;
        this.supplierPaymentMapper = supplierPaymentMapper;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
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
