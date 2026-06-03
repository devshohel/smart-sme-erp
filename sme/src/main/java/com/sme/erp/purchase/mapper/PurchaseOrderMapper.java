package com.sme.erp.purchase.mapper;

import com.sme.erp.purchase.dto.PurchaseOrderDTO;
import com.sme.erp.purchase.entity.PurchaseOrder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PurchaseOrderMapper {

    private final PurchaseItemMapper purchaseItemMapper;

    public PurchaseOrderMapper(PurchaseItemMapper purchaseItemMapper) {
        this.purchaseItemMapper = purchaseItemMapper;
    }

    public PurchaseOrderDTO toDTO(PurchaseOrder entity) {
        if (entity == null) {
            return null;
        }

        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(entity.getId());
        dto.setPurchaseCode(entity.getPurchaseCode());
        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierName(entity.getSupplier().getName());
        }
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
            dto.setWarehouseName(entity.getWarehouse().getName());
        }
        dto.setPurchaseDate(entity.getPurchaseDate());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setDiscountAmount(entity.getDiscountAmount());
        dto.setTaxAmount(entity.getTaxAmount());
        dto.setNetTotal(entity.getNetTotal());
        dto.setPaidAmount(entity.getPaidAmount());
        dto.setDueAmount(entity.getDueAmount());
        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setItems(entity.getItems().stream().map(purchaseItemMapper::toDTO).collect(Collectors.toList()));
        return dto;
    }
}
