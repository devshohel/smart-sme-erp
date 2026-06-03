package com.sme.erp.supplier.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
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

    public SupplierServiceImpl(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
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

        // TODO Supplier ledger integration should own future payable and advance balance movements.

        return supplierMapper.toDTO(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierDTO update(Long id, SupplierDTO dto) {
        normalizeDto(dto);
        validateFinancials(dto);

        Supplier supplier = findSupplierById(id);
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

        return supplierMapper.toDTO(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        supplierRepository.delete(findSupplierById(id));
    }

    private void normalizeDto(SupplierDTO dto) {
        dto.setSupplierCode(RequestValueUtils.normalize(dto.getSupplierCode()));
        dto.setName(RequestValueUtils.normalizeRequired(dto.getName(), "Supplier name"));
        dto.setCompanyName(RequestValueUtils.normalize(dto.getCompanyName()));
        dto.setContactPerson(RequestValueUtils.normalize(dto.getContactPerson()));
        dto.setPhone(RequestValueUtils.normalize(dto.getPhone()));
        dto.setEmail(RequestValueUtils.normalize(dto.getEmail()));
        dto.setAddress(RequestValueUtils.normalize(dto.getAddress()));
        dto.setCity(RequestValueUtils.normalize(dto.getCity()));
        dto.setCountry(RequestValueUtils.normalize(dto.getCountry()));
        dto.setPostalCode(RequestValueUtils.normalize(dto.getPostalCode()));
        dto.setTaxNumber(RequestValueUtils.normalize(dto.getTaxNumber()));
        dto.setBankAccount(RequestValueUtils.normalize(dto.getBankAccount()));
        dto.setPaymentTerms(RequestValueUtils.normalize(dto.getPaymentTerms()));
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

        long nextNumber = supplierRepository.findTopByOrderByIdDesc()
                .map(supplier -> supplier.getId() + 1)
                .orElse(1L);
        String generated = String.format("SUP-%04d", nextNumber);
        while (supplierRepository.existsBySupplierCode(generated)) {
            nextNumber++;
            generated = String.format("SUP-%04d", nextNumber);
        }
        return generated;
    }

    private void validateSupplierCodeUnique(String supplierCode, Long currentId) {
        boolean exists = currentId == null
                ? supplierRepository.existsBySupplierCode(supplierCode)
                : supplierRepository.existsBySupplierCodeAndIdNot(supplierCode, currentId);
        if (exists) {
            throw new DuplicateResourceException("Supplier code already exists: " + supplierCode);
        }
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
