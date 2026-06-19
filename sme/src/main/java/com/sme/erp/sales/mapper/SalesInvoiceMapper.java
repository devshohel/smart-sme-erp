package com.sme.erp.sales.mapper;

import com.sme.erp.sales.dto.SalesInvoiceDTO;
import com.sme.erp.sales.entity.SalesInvoice;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SalesInvoiceMapper {

    private final SalesItemMapper salesItemMapper;

    public SalesInvoiceMapper(SalesItemMapper salesItemMapper) {
        this.salesItemMapper = salesItemMapper;
    }

    public SalesInvoiceDTO toDTO(SalesInvoice entity) {
        if (entity == null) {
            return null;
        }

        SalesInvoiceDTO dto = new SalesInvoiceDTO();
        dto.setId(entity.getId());
        dto.setInvoiceNo(entity.getInvoiceNo());
        if (entity.getOrder() != null) {
            dto.setOrderId(entity.getOrder().getId());
            dto.setOrderNo(entity.getOrder().getOrderNo());
        }
        if (entity.getCustomer() != null) {
            dto.setCustomerId(entity.getCustomer().getId());
            dto.setCustomerName(entity.getCustomer().getName());
        }
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
            dto.setWarehouseName(entity.getWarehouse().getName());
        }
        dto.setSaleDate(entity.getSaleDate());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setDiscountAmount(entity.getDiscountAmount());
        dto.setTaxAmount(entity.getTaxAmount());
        dto.setNetTotal(entity.getNetTotal());
        dto.setPaidAmount(entity.getPaidAmount());
        dto.setDueAmount(entity.getDueAmount());
        dto.setPaymentStatus(entity.getPaymentStatus());
        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setNotes(entity.getNotes());
        dto.setSubmittedAt(entity.getSubmittedAt());
        dto.setSubmittedBy(entity.getSubmittedBy());
        dto.setApprovedAt(entity.getApprovedAt());
        dto.setApprovedBy(entity.getApprovedBy());
        dto.setPostedAt(entity.getPostedAt());
        dto.setPostedBy(entity.getPostedBy());
        dto.setCancelledAt(entity.getCancelledAt());
        dto.setCancelledBy(entity.getCancelledBy());
        dto.setReversedAt(entity.getReversedAt());
        dto.setReversedBy(entity.getReversedBy());
        dto.setReversalReason(entity.getReversalReason());
        dto.setItems(entity.getItems().stream().map(salesItemMapper::toDTO).collect(Collectors.toList()));
        return dto;
    }
}
