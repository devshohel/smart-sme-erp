package com.sme.erp.sales.mapper;

import com.sme.erp.product.entity.Uom;
import com.sme.erp.sales.dto.SalesItemDTO;
import com.sme.erp.sales.entity.SalesItem;
import org.springframework.stereotype.Component;

@Component
public class SalesItemMapper {

    public SalesItemDTO toDTO(SalesItem entity) {
        if (entity == null) {
            return null;
        }

        SalesItemDTO dto = new SalesItemDTO();
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
        return dto;
    }
}
