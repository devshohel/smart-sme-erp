package com.sme.erp.customer.receipt.mapper;

import com.sme.erp.customer.receipt.dto.CustomerReceiptDTO;
import com.sme.erp.customer.receipt.entity.CustomerReceipt;
import org.springframework.stereotype.Component;

@Component
public class CustomerReceiptMapper {

    public CustomerReceiptDTO toDTO(CustomerReceipt entity) {
        if (entity == null) {
            return null;
        }

        CustomerReceiptDTO dto = new CustomerReceiptDTO();
        dto.setId(entity.getId());
        dto.setReceiptNo(entity.getReceiptNo());
        if (entity.getCustomer() != null) {
            dto.setCustomerId(entity.getCustomer().getId());
            dto.setCustomerCode(entity.getCustomer().getCustomerCode());
            dto.setCustomerName(entity.getCustomer().getName());
        }
        dto.setReceiptDate(entity.getReceiptDate());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setAmount(entity.getAmount());
        dto.setReferenceNo(entity.getReferenceNo());
        dto.setNotes(entity.getNotes());
        dto.setStatus(entity.getStatus());
        dto.setPostedAt(entity.getPostedAt());
        dto.setCancelledAt(entity.getCancelledAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
