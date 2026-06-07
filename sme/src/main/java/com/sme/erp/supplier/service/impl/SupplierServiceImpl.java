package com.sme.erp.supplier.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.enums.Status;
import com.sme.erp.supplier.dto.SupplierDTO;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.mapper.SupplierMapper;
import com.sme.erp.supplier.repository.SupplierRepository;
import com.sme.erp.supplier.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;

    public SupplierServiceImpl(
            SupplierRepository supplierRepository,
            SupplierMapper supplierMapper,
            ActivityLogService activityLogService,
            AuditLogService auditLogService) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> getAll(String keyword, Status status) {
        String normalizedKeyword = RequestValueUtils.normalize(keyword);
        return supplierRepository.search(normalizedKeyword, status)
                .stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO getById(Long id) {
        return supplierMapper.toDTO(findSupplierById(id));
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
        if (dto.getCurrentBalance() != null) {
            supplier.setCurrentBalance(dto.getCurrentBalance());
        }
        if (supplier.getStatus() == null) {
            supplier.setStatus(Status.ACTIVE);
        }

        SupplierDTO saved = supplierMapper.toDTO(supplierRepository.save(supplier));
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
}
