package com.sme.erp.purchase.mapper;

import com.sme.erp.purchase.dto.PurchaseReturnItemDTO;
import com.sme.erp.purchase.entity.PurchaseReturnItem;
import org.springframework.stereotype.Component;

@Component
public class PurchaseReturnItemMapper {

    public PurchaseReturnItemDTO toDTO(PurchaseReturnItem entity) {
        if (entity == null) {
            return null;
        }

        PurchaseReturnItemDTO dto = new PurchaseReturnItemDTO();
        dto.setId(entity.getId());
        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getProductName());
        }
        dto.setQuantity(entity.getQuantity());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setTotal(entity.getTotal());
        return dto;
    }
}
