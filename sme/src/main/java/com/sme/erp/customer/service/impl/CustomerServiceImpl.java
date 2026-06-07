package com.sme.erp.customer.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.customer.dto.CustomerDTO;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.mapper.CustomerMapper;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.customer.service.CustomerService;
import com.sme.erp.enums.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper,
            ActivityLogService activityLogService,
            AuditLogService auditLogService) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAll(String keyword, Status status) {
        String normalizedKeyword = RequestValueUtils.normalize(keyword);
        return customerRepository.search(normalizedKeyword, status)
                .stream()
                .map(customerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getById(Long id) {
        return customerMapper.toDTO(findCustomerById(id));
    }

    @Override
    @Transactional
    public CustomerDTO create(CustomerDTO dto) {
        normalizeDto(dto);
        validateFinancials(dto);

        Customer customer = new Customer();
        customer.setCustomerCode(resolveCustomerCodeForCreate(dto.getCustomerCode()));
        validateEmailUnique(dto.getEmail(), null);
        validatePhoneUnique(dto.getPhone(), null);

        customerMapper.updateEntity(dto, customer);
        customer.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        customer.setCreditLimit(safeNonNegative(dto.getCreditLimit(), "Credit limit cannot be negative"));
        customer.setOpeningBalance(safeNonNegative(dto.getOpeningBalance(), "Opening balance cannot be negative"));
        customer.setCurrentBalance(customer.getOpeningBalance());
        customer.setIsDeleted(false);

        // Customer balance movements remain isolated from accounting ledger postings in this phase.

        CustomerDTO saved = customerMapper.toDTO(customerRepository.save(customer));
        activityLogService.log("CUSTOMER_CREATE", "CUSTOMER", "customers", saved.getId(), "Created customer " + saved.getName());
        auditLogService.log("customers", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public CustomerDTO update(Long id, CustomerDTO dto) {
        normalizeDto(dto);
        validateFinancials(dto);

        Customer customer = findCustomerById(id);
        CustomerDTO oldData = customerMapper.toDTO(customer);
        String requestedCustomerCode = dto.getCustomerCode();
        if (requestedCustomerCode != null) {
            validateCustomerCodeUnique(requestedCustomerCode, id);
            customer.setCustomerCode(requestedCustomerCode);
        }
        validateEmailUnique(dto.getEmail(), id);
        validatePhoneUnique(dto.getPhone(), id);

        BigDecimal existingOpeningBalance = customer.getOpeningBalance() != null ? customer.getOpeningBalance() : BigDecimal.ZERO;
        customerMapper.updateEntity(dto, customer);
        customer.setCreditLimit(dto.getCreditLimit() != null
                ? safeNonNegative(dto.getCreditLimit(), "Credit limit cannot be negative")
                : safeNonNegative(customer.getCreditLimit(), "Credit limit cannot be negative"));
        customer.setOpeningBalance(dto.getOpeningBalance() != null
                ? safeNonNegative(dto.getOpeningBalance(), "Opening balance cannot be negative")
                : existingOpeningBalance);
        if (dto.getCurrentBalance() != null) {
            customer.setCurrentBalance(dto.getCurrentBalance());
        }
        if (customer.getStatus() == null) {
            customer.setStatus(Status.ACTIVE);
        }

        CustomerDTO saved = customerMapper.toDTO(customerRepository.save(customer));
        auditLogService.log("customers", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = findCustomerById(id);
        CustomerDTO oldData = customerMapper.toDTO(customer);
        customerRepository.delete(customer);
        auditLogService.log("customers", id, auditLogService.toJson(oldData), null, "DELETE");
    }

    private void normalizeDto(CustomerDTO dto) {
        dto.setCustomerCode(RequestValueUtils.normalize(dto.getCustomerCode()));
        dto.setName(RequestValueUtils.normalizeRequired(dto.getName(), "Customer name"));
        dto.normalizeCompanyName(RequestValueUtils.normalize(dto.getCompanyName()));
        dto.normalizeContactPerson(RequestValueUtils.normalize(dto.getContactPerson()));
        dto.normalizePhone(RequestValueUtils.normalize(dto.getPhone()));
        dto.normalizeEmail(RequestValueUtils.normalize(dto.getEmail()));
        dto.normalizeAddress(RequestValueUtils.normalize(dto.getAddress()));
        dto.normalizeCity(RequestValueUtils.normalize(dto.getCity()));
        dto.normalizeCountry(RequestValueUtils.normalize(dto.getCountry()));
        dto.normalizePostalCode(RequestValueUtils.normalize(dto.getPostalCode()));
        dto.normalizeTaxNumber(RequestValueUtils.normalize(dto.getTaxNumber()));
    }

    private void validateFinancials(CustomerDTO dto) {
        safeNonNegative(dto.getCreditLimit(), "Credit limit cannot be negative");
        safeNonNegative(dto.getOpeningBalance(), "Opening balance cannot be negative");
    }

    private BigDecimal safeNonNegative(BigDecimal value, String message) {
        BigDecimal normalized = value != null ? value : BigDecimal.ZERO;
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(message);
        }
        return normalized;
    }

    private String resolveCustomerCodeForCreate(String requestedCode) {
        if (requestedCode != null) {
            validateCustomerCodeUnique(requestedCode, null);
            return requestedCode;
        }

        long nextNumber = customerRepository.findMaxIdIncludingDeleted() + 1;
        String generated = String.format("CUS-%04d", nextNumber);
        while (existsByCustomerCodeIncludingDeleted(generated)) {
            nextNumber++;
            generated = String.format("CUS-%04d", nextNumber);
        }
        return generated;
    }

    private void validateCustomerCodeUnique(String customerCode, Long currentId) {
        boolean exists = currentId == null
                ? existsByCustomerCodeIncludingDeleted(customerCode)
                : existsByCustomerCodeAndIdNotIncludingDeleted(customerCode, currentId);
        if (exists) {
            throw new DuplicateResourceException("Customer code already exists: " + customerCode);
        }
    }

    private boolean existsByCustomerCodeIncludingDeleted(String customerCode) {
        return hasRows(customerRepository.countByCustomerCodeIncludingDeleted(customerCode));
    }

    private boolean existsByCustomerCodeAndIdNotIncludingDeleted(String customerCode, Long currentId) {
        return hasRows(customerRepository.countByCustomerCodeAndIdNotIncludingDeleted(customerCode, currentId));
    }

    private boolean hasRows(Long count) {
        return count != null && count > 0;
    }

    private void validateEmailUnique(String email, Long currentId) {
        if (email == null) {
            return;
        }
        boolean exists = currentId == null
                ? customerRepository.existsByEmail(email)
                : customerRepository.existsByEmailAndIdNot(email, currentId);
        if (exists) {
            throw new DuplicateResourceException("Customer email already exists: " + email);
        }
    }

    private void validatePhoneUnique(String phone, Long currentId) {
        if (phone == null) {
            return;
        }
        boolean exists = currentId == null
                ? customerRepository.existsByPhone(phone)
                : customerRepository.existsByPhoneAndIdNot(phone, currentId);
        if (exists) {
            throw new DuplicateResourceException("Customer phone already exists: " + phone);
        }
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }
}
