package com.sme.erp.sales.mapper;

import com.sme.erp.product.entity.Uom;
import com.sme.erp.sales.dto.SalesItemDTO;
import com.sme.erp.sales.entity.SalesItem;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class SalesItemMapper {

    public SalesItemDTO toDTO(SalesItem entity) {
        if (entity == null) {
            return null;
        }

        SalesItemDTO dto = new SalesItemDTO();
        dto.setId(entity.getId());
        mapProduct(entity, dto);
        mapUom(entity, dto);

        dto.setQuantity(entity.getQuantity());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setDiscount(entity.getDiscount());
        dto.setTax(entity.getTax());
        dto.setSubTotal(entity.getSubTotal());
        return dto;
    }

    private void mapProduct(SalesItem entity, SalesItemDTO dto) {
        try {
            if (entity.getProduct() != null) {
                dto.setProductId(entity.getProduct().getId());
                dto.setProductName(entity.getProduct().getProductName());
            }
        } catch (EntityNotFoundException | ObjectNotFoundException ignored) {
            dto.setProductId(null);
            dto.setProductName("Deleted product");
        }
    }

    private void mapUom(SalesItem entity, SalesItemDTO dto) {
        try {
            Uom uom = entity.getUom();
            if (uom != null) {
                dto.setUomId(uom.getId());
                dto.setUomName(uom.getName());
            }
        } catch (EntityNotFoundException | ObjectNotFoundException ignored) {
            dto.setUomId(null);
            dto.setUomName(null);
        }
    }
}
