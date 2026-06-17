package com.sme.erp.customer.receipt.mapper;

import com.sme.erp.customer.receipt.dto.CustomerReceiptAllocationDTO;
import com.sme.erp.customer.receipt.entity.CustomerReceiptAllocation;
import org.springframework.stereotype.Component;

@Component
public class CustomerReceiptAllocationMapper {

    public CustomerReceiptAllocationDTO toDTO(CustomerReceiptAllocation entity) {
        if (entity == null) {
            return null;
        }

        CustomerReceiptAllocationDTO dto = new CustomerReceiptAllocationDTO();
        dto.setId(entity.getId());
        if (entity.getSalesInvoice() != null) {
            dto.setSalesInvoiceId(entity.getSalesInvoice().getId());
            dto.setInvoiceNo(entity.getSalesInvoice().getInvoiceNo());
            dto.setInvoiceDate(entity.getSalesInvoice().getSaleDate());
            dto.setNetTotal(entity.getSalesInvoice().getNetTotal());
            dto.setPaidAmount(entity.getSalesInvoice().getPaidAmount());
            dto.setDueAmount(entity.getSalesInvoice().getDueAmount());
        }
        dto.setAllocatedAmount(entity.getAllocatedAmount());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
