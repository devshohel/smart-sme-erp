package com.sme.erp.customer.mapper;

import com.sme.erp.customer.dto.CustomerDTO;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.enums.Status;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CustomerMapper {

    public CustomerDTO toDTO(Customer entity) {
        if (entity == null) {
            return null;
        }

        CustomerDTO dto = new CustomerDTO();
        dto.setId(entity.getId());
        dto.setCustomerCode(entity.getCustomerCode());
        dto.setName(entity.getName());
        dto.setCompanyName(entity.getCompanyName());
        dto.setContactPerson(entity.getContactPerson());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setAddress(entity.getAddress());
        dto.setCity(entity.getCity());
        dto.setCountry(entity.getCountry());
        dto.setPostalCode(entity.getPostalCode());
        dto.setCreditLimit(entity.getCreditLimit() != null ? entity.getCreditLimit() : BigDecimal.ZERO);
        BigDecimal openingBalance = entity.getOpeningBalance() != null ? entity.getOpeningBalance() : BigDecimal.ZERO;
        dto.setOpeningBalance(openingBalance);
        dto.setCurrentBalance(entity.getCurrentBalance() != null ? entity.getCurrentBalance() : openingBalance);
        dto.setTaxNumber(entity.getTaxNumber());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus() : Status.ACTIVE);
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public Customer toEntity(CustomerDTO dto) {
        return updateEntity(dto, new Customer());
    }

    public Customer updateEntity(CustomerDTO dto, Customer entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        if (dto.getCustomerCode() != null) {
            entity.setCustomerCode(dto.getCustomerCode());
        }
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getCompanyName() != null) {
            entity.setCompanyName(dto.getCompanyName());
        }
        if (dto.getContactPerson() != null) {
            entity.setContactPerson(dto.getContactPerson());
        }
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getAddress() != null) {
            entity.setAddress(dto.getAddress());
        }
        if (dto.getCity() != null) {
            entity.setCity(dto.getCity());
        }
        if (dto.getCountry() != null) {
            entity.setCountry(dto.getCountry());
        }
        if (dto.getPostalCode() != null) {
            entity.setPostalCode(dto.getPostalCode());
        }
        if (dto.getCreditLimit() != null) {
            entity.setCreditLimit(dto.getCreditLimit());
        }
        if (dto.getOpeningBalance() != null) {
            entity.setOpeningBalance(dto.getOpeningBalance());
        }
        if (dto.getCurrentBalance() != null) {
            entity.setCurrentBalance(dto.getCurrentBalance());
        }
        if (dto.getTaxNumber() != null) {
            entity.setTaxNumber(dto.getTaxNumber());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getCreatedBy() != null) {
            entity.setCreatedBy(dto.getCreatedBy());
        }

        return entity;
    }
}
