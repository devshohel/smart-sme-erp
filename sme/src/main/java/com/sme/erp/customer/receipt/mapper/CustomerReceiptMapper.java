package com.sme.erp.customer.receipt.mapper;

import com.sme.erp.customer.receipt.dto.CustomerReceiptDTO;
import com.sme.erp.customer.receipt.entity.CustomerReceipt;
import com.sme.erp.customer.receipt.entity.CustomerReceiptAllocation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerReceiptMapper {
    private final CustomerReceiptAllocationMapper allocationMapper;

    public CustomerReceiptMapper(CustomerReceiptAllocationMapper allocationMapper) {
        this.allocationMapper = allocationMapper;
    }

    public CustomerReceiptDTO toDTO(CustomerReceipt entity) {
        return toDTO(entity, true);
    }

    public CustomerReceiptDTO toDTO(CustomerReceipt entity, boolean includeAllocations) {
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
        dto.setAllocationMode(entity.getAllocationMode());
        dto.setTotalAllocatedAmount(normalize(entity.getTotalAllocatedAmount()));
        dto.setUnappliedAmount(normalize(entity.getUnappliedAmount()));
        if (includeAllocations) {
            List<CustomerReceiptAllocation> allocations = entity.getAllocations();
            dto.setAllocations(allocations == null
                    ? List.of()
                    : allocations.stream().map(allocationMapper::toDTO).collect(Collectors.toList()));
        } else {
            dto.setAllocations(List.of());
        }
        dto.setStatus(entity.getStatus());
        dto.setPostedAt(entity.getPostedAt());
        dto.setCancelledAt(entity.getCancelledAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
