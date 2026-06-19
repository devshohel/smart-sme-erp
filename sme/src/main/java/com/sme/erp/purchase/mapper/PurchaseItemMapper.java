package com.sme.erp.purchase.mapper;

import com.sme.erp.product.entity.Uom;
import com.sme.erp.purchase.dto.PurchaseItemDTO;
import com.sme.erp.purchase.entity.PurchaseItem;
import org.springframework.stereotype.Component;

@Component
public class PurchaseItemMapper {

    public PurchaseItemDTO toDTO(PurchaseItem entity) {
        if (entity == null) {
            return null;
        }

        PurchaseItemDTO dto = new PurchaseItemDTO();
        dto.setId(entity.getId());
        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getProductName());
        }

        Uom uom = entity.getUom();
        if (uom != null) {
            dto.setUomId(uom.getId());
            dto.setUomName(uom.getName());
        }

        dto.setQuantity(entity.getQuantity());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setDiscount(entity.getDiscount());
        dto.setTax(entity.getTax());
        dto.setSubTotal(entity.getSubTotal());
        dto.setReceivedQuantity(entity.getReceivedQuantity());
        dto.setReturnedQuantity(entity.getReturnedQuantity());
        return dto;
    }
}
