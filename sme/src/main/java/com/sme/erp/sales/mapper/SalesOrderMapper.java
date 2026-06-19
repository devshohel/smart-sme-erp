package com.sme.erp.sales.mapper;

import com.sme.erp.sales.dto.SalesOrderDTO;
import com.sme.erp.sales.entity.SalesOrder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SalesOrderMapper {

    private final SalesItemMapper salesItemMapper;

    public SalesOrderMapper(SalesItemMapper salesItemMapper) {
        this.salesItemMapper = salesItemMapper;
    }

    public SalesOrderDTO toDTO(SalesOrder entity) {
        if (entity == null) {
            return null;
        }

        SalesOrderDTO dto = new SalesOrderDTO();
        dto.setId(entity.getId());
        dto.setOrderNo(entity.getOrderNo());
        if (entity.getCustomer() != null) {
            dto.setCustomerId(entity.getCustomer().getId());
            dto.setCustomerName(entity.getCustomer().getName());
        }
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
            dto.setWarehouseName(entity.getWarehouse().getName());
        }
        dto.setOrderDate(entity.getOrderDate());
        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setNotes(entity.getNotes());
        dto.setSubmittedAt(entity.getSubmittedAt());
        dto.setSubmittedBy(entity.getSubmittedBy());
        dto.setApprovedAt(entity.getApprovedAt());
        dto.setApprovedBy(entity.getApprovedBy());
        dto.setRejectedAt(entity.getRejectedAt());
        dto.setRejectedBy(entity.getRejectedBy());
        dto.setRejectionReason(entity.getRejectionReason());
        dto.setCancelledAt(entity.getCancelledAt());
        dto.setCancelledBy(entity.getCancelledBy());
        dto.setConvertedAt(entity.getConvertedAt());
        dto.setConvertedBy(entity.getConvertedBy());
        dto.setGrandTotal(entity.getGrandTotal());
        dto.setItems(entity.getItems().stream().map(salesItemMapper::toDTO).collect(Collectors.toList()));
        return dto;
    }
}
