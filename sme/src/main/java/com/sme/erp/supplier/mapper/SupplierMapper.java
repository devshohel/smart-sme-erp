package com.sme.erp.supplier.mapper;

import com.sme.erp.enums.Status;
import com.sme.erp.supplier.dto.SupplierDTO;
import com.sme.erp.supplier.entity.Supplier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SupplierMapper {

    public SupplierDTO toDTO(Supplier entity) {
        if (entity == null) {
            return null;
        }

        SupplierDTO dto = new SupplierDTO();
        dto.setId(entity.getId());
        dto.setSupplierCode(entity.getSupplierCode());
        dto.setName(entity.getName());
        dto.setCompanyName(entity.getCompanyName());
        dto.setContactPerson(entity.getContactPerson());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setAddress(entity.getAddress());
        dto.setCity(entity.getCity());
        dto.setCountry(entity.getCountry());
        dto.setPostalCode(entity.getPostalCode());
        BigDecimal openingBalance = entity.getOpeningBalance() != null ? entity.getOpeningBalance() : BigDecimal.ZERO;
        dto.setOpeningBalance(openingBalance);
        dto.setCurrentBalance(entity.getCurrentBalance() != null ? entity.getCurrentBalance() : openingBalance);
        dto.setTaxNumber(entity.getTaxNumber());
        dto.setBankAccount(entity.getBankAccount());
        dto.setPaymentTerms(entity.getPaymentTerms());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus() : Status.ACTIVE);
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public Supplier toEntity(SupplierDTO dto) {
        return updateEntity(dto, new Supplier());
    }

    public Supplier updateEntity(SupplierDTO dto, Supplier entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        if (dto.getSupplierCode() != null) {
            entity.setSupplierCode(dto.getSupplierCode());
        }
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.isCompanyNamePresent()) {
            entity.setCompanyName(dto.getCompanyName());
        }
        if (dto.isContactPersonPresent()) {
            entity.setContactPerson(dto.getContactPerson());
        }
        if (dto.isPhonePresent()) {
            entity.setPhone(dto.getPhone());
        }
        if (dto.isEmailPresent()) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.isAddressPresent()) {
            entity.setAddress(dto.getAddress());
        }
        if (dto.isCityPresent()) {
            entity.setCity(dto.getCity());
        }
        if (dto.isCountryPresent()) {
            entity.setCountry(dto.getCountry());
        }
        if (dto.isPostalCodePresent()) {
            entity.setPostalCode(dto.getPostalCode());
        }
        if (dto.getOpeningBalance() != null) {
            entity.setOpeningBalance(dto.getOpeningBalance());
        }
        if (dto.isTaxNumberPresent()) {
            entity.setTaxNumber(dto.getTaxNumber());
        }
        if (dto.isBankAccountPresent()) {
            entity.setBankAccount(dto.getBankAccount());
        }
        if (dto.isPaymentTermsPresent()) {
            entity.setPaymentTerms(dto.getPaymentTerms());
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
