package com.sme.erp.inventory.mapper;

import com.sme.erp.inventory.dto.StockMovementDTO;
import com.sme.erp.inventory.entity.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementDTO toDTO(StockMovement movement) {
        if (movement == null) {
            return null;
        }

        StockMovementDTO dto = new StockMovementDTO();
        dto.setId(movement.getId());
        dto.setMovementCode(movement.getMovementCode());
        dto.setMovementType(movement.getMovementType());
        dto.setQuantity(movement.getQuantity());
        dto.setQuantityBefore(movement.getQuantityBefore());
        dto.setQuantityChange(movement.getQuantityChange());
        dto.setQuantityAfter(movement.getQuantityAfter());
        dto.setUnitCost(movement.getUnitCost());
        dto.setReferenceType(movement.getReferenceType());
        dto.setReferenceNo(movement.getReferenceNo());
        dto.setBatchNo(movement.getBatchNo());
        dto.setExpiryDate(movement.getExpiryDate());
        dto.setNote(movement.getNote());
        dto.setCreatedAt(movement.getCreatedAt());

        if (movement.getProduct() != null) {
            dto.setProductId(movement.getProduct().getId());
            dto.setProductCode(movement.getProduct().getProductCode());
            dto.setProductName(movement.getProduct().getProductName());

            StockMovementDTO.ProductRefDTO productRef = new StockMovementDTO.ProductRefDTO();
            productRef.setId(movement.getProduct().getId());
            productRef.setProductName(movement.getProduct().getProductName());
            dto.setProduct(productRef);
        }

        if (movement.getWarehouse() != null) {
            dto.setWarehouseId(movement.getWarehouse().getId());
            dto.setWarehouseCode(movement.getWarehouse().getWarehouseCode());
            dto.setWarehouseName(movement.getWarehouse().getName());

            StockMovementDTO.WarehouseRefDTO warehouseRef = new StockMovementDTO.WarehouseRefDTO();
            warehouseRef.setId(movement.getWarehouse().getId());
            warehouseRef.setWarehouseCode(movement.getWarehouse().getWarehouseCode());
            warehouseRef.setWarehouseName(movement.getWarehouse().getName());
            dto.setWarehouse(warehouseRef);
        }

        return dto;
    }
}
