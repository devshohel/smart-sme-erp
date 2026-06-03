package com.sme.erp.sales.mapper;

import com.sme.erp.sales.dto.SalesReturnItemDTO;
import com.sme.erp.sales.entity.SalesReturnItem;
import org.springframework.stereotype.Component;

@Component
public class SalesReturnItemMapper {

    public SalesReturnItemDTO toDTO(SalesReturnItem entity) {
        if (entity == null) {
            return null;
        }

        SalesReturnItemDTO dto = new SalesReturnItemDTO();
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
