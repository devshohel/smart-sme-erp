package com.sme.erp.inventory.mapper;

import com.sme.erp.inventory.dto.StockDTO;
import com.sme.erp.inventory.entity.Stock;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {

    public StockDTO toDTO(Stock stock) {
        if (stock == null) return null;

        StockDTO dto = new StockDTO();
        dto.setId(stock.getId());
        dto.setQuantity(stock.getQuantity());
        dto.setReorderLevel(stock.getReorderLevel());

        if (stock.getProduct() != null) {
            dto.setProductId(stock.getProduct().getId());
            dto.setProductName(stock.getProduct().getProductName());
        }

        if (stock.getWarehouse() != null) {
            dto.setWarehouseId(stock.getWarehouse().getId());
            dto.setWarehouseName(stock.getWarehouse().getName());
        }

        return dto;
    }
}
