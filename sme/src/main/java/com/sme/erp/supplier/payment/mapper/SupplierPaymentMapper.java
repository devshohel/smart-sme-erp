package com.sme.erp.supplier.payment.mapper;

import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.payment.dto.SupplierPaymentDTO;
import com.sme.erp.supplier.payment.entity.SupplierPayment;
import com.sme.erp.supplier.payment.entity.SupplierPaymentAllocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupplierPaymentMapper {
    private final SupplierPaymentAllocationMapper allocationMapper;

    public SupplierPaymentMapper(SupplierPaymentAllocationMapper allocationMapper) {
        this.allocationMapper = allocationMapper;
    }

    public SupplierPaymentDTO toDTO(SupplierPayment entity) {
        return toDTO(entity, false);
    }

    public SupplierPaymentDTO toDTO(SupplierPayment entity, boolean includeAllocations) {
        SupplierPaymentDTO dto = new SupplierPaymentDTO();
        dto.setId(entity.getId());
        dto.setPaymentNo(entity.getPaymentNo());
        Supplier supplier = entity.getSupplier();
        if (supplier != null) {
            dto.setSupplierId(supplier.getId());
            dto.setSupplierCode(supplier.getSupplierCode());
            dto.setSupplierName(supplier.getName());
        }
        dto.setPaymentDate(entity.getPaymentDate());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setAmount(entity.getAmount());
        dto.setReferenceNo(entity.getReferenceNo());
        dto.setNotes(entity.getNotes());
        dto.setAllocationMode(entity.getAllocationMode());
        dto.setTotalAllocatedAmount(entity.getTotalAllocatedAmount());
        dto.setUnappliedAmount(entity.getUnappliedAmount());
        dto.setStatus(entity.getStatus());
        dto.setPostedAt(entity.getPostedAt());
        dto.setCancelledAt(entity.getCancelledAt());
        dto.setReversedAt(entity.getReversedAt());
        dto.setReversalReason(entity.getReversalReason());
        dto.setReversedBy(entity.getReversedBy());
        dto.setCanReverse(entity.getStatus() == com.sme.erp.supplier.payment.enums.SupplierPaymentStatus.POSTED
                && entity.getReversedAt() == null);
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        if (includeAllocations) {
            List<SupplierPaymentAllocation> allocations = entity.getAllocations();
            dto.setAllocations(allocations == null ? List.of() : allocations.stream()
                    .map(allocationMapper::toDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
