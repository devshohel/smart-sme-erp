package com.sme.erp.purchase.mapper;

import com.sme.erp.purchase.dto.PurchaseReturnDTO;
import com.sme.erp.purchase.entity.PurchaseReturn;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PurchaseReturnMapper {

    private final PurchaseReturnItemMapper purchaseReturnItemMapper;

    public PurchaseReturnMapper(PurchaseReturnItemMapper purchaseReturnItemMapper) {
        this.purchaseReturnItemMapper = purchaseReturnItemMapper;
    }

    public PurchaseReturnDTO toDTO(PurchaseReturn entity) {
        if (entity == null) {
            return null;
        }

        PurchaseReturnDTO dto = new PurchaseReturnDTO();
        dto.setId(entity.getId());
        dto.setReturnCode(entity.getReturnCode());
        if (entity.getPurchase() != null) {
            dto.setPurchaseId(entity.getPurchase().getId());
            dto.setPurchaseCode(entity.getPurchase().getPurchaseCode());
        }
        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierName(entity.getSupplier().getName());
        }
        dto.setReturnDate(entity.getReturnDate());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setItems(entity.getItems().stream().map(purchaseReturnItemMapper::toDTO).collect(Collectors.toList()));
        return dto;
    }
}
