package com.sme.erp.inventory.service;

import java.util.List;
import java.math.BigDecimal;
import com.sme.erp.inventory.dto.StockDTO;
import com.sme.erp.inventory.dto.StockMovementDTO;

public interface StockService {

	StockDTO stockIn(Long productId, Long warehouseId, BigDecimal qty, BigDecimal unitCost);

    StockDTO stockIn(
            Long productId,
            Long warehouseId,
            BigDecimal qty,
            BigDecimal unitCost,
            String referenceType,
            String referenceNo);

    StockDTO stockOut(Long productId, Long warehouseId, BigDecimal  qty);

    StockDTO stockOut(Long productId, Long warehouseId, BigDecimal qty, String referenceType, String referenceNo);
    
    StockDTO adjustStock(Long productId, Long warehouseId, BigDecimal qty, String reason);

    StockDTO getStock(Long productId, Long warehouseId);
    
    List<StockDTO> getAllStock(); 

    List<StockMovementDTO> getAllMovements();
}
