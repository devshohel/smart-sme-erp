package com.sme.erp.sales.mapper;

import com.sme.erp.sales.dto.SalesReturnItemDTO;
import com.sme.erp.sales.entity.SalesReturnItem;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class SalesReturnItemMapper {

    public SalesReturnItemDTO toDTO(SalesReturnItem entity) {
        if (entity == null) {
            return null;
        }

        SalesReturnItemDTO dto = new SalesReturnItemDTO();
        dto.setId(entity.getId());
        mapProduct(entity, dto);
        dto.setInvoiceItemId(entity.getInvoiceItemId());
        dto.setQuantity(entity.getQuantity());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setDiscount(entity.getDiscount());
        dto.setTax(entity.getTax());
        dto.setReturnReason(entity.getReturnReason());
        dto.setCondition(entity.getCondition());
        dto.setRestock(entity.getRestock());
        dto.setTotal(entity.getTotal());
        return dto;
    }

    private void mapProduct(SalesReturnItem entity, SalesReturnItemDTO dto) {
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
}
