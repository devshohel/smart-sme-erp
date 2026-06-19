package com.sme.erp.purchase.mapper;

import com.sme.erp.purchase.dto.PurchaseReceiveDTO;
import com.sme.erp.purchase.dto.PurchaseReceiveItemDTO;
import com.sme.erp.purchase.entity.GoodsReceiveItem;
import com.sme.erp.purchase.entity.GoodsReceiveNote;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PurchaseReceiveMapper {

    public PurchaseReceiveDTO toDTO(GoodsReceiveNote entity) {
        if (entity == null) {
            return null;
        }
        PurchaseReceiveDTO dto = new PurchaseReceiveDTO();
        dto.setId(entity.getId());
        dto.setGrnNo(entity.getGrnNo());
        if (entity.getPurchaseOrder() != null) {
            dto.setPurchaseOrderId(entity.getPurchaseOrder().getId());
            dto.setPurchaseCode(entity.getPurchaseOrder().getPurchaseCode());
        }
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
            dto.setWarehouseName(entity.getWarehouse().getName());
        }
        dto.setReceiveDate(entity.getReceiveDate());
        dto.setStatus(entity.getStatus());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setPostedAt(entity.getPostedAt());
        dto.setItems(entity.getItems().stream().map(this::toItemDTO).collect(Collectors.toList()));
        return dto;
    }

    private PurchaseReceiveItemDTO toItemDTO(GoodsReceiveItem item) {
        PurchaseReceiveItemDTO dto = new PurchaseReceiveItemDTO();
        dto.setId(item.getId());
        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getProductName());
        }
        dto.setOrderedQty(item.getOrderedQty());
        dto.setReceivedQty(item.getReceivedQty());
        dto.setRemainingQty(item.getRemainingQty());
        return dto;
    }
}
