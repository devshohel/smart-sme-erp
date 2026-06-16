package com.sme.erp.inventory.service;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.sme.erp.enums.MovementType;
import com.sme.erp.inventory.dto.StockCardDTO;
import com.sme.erp.inventory.dto.StockDTO;
import com.sme.erp.inventory.dto.StockMovementDTO;
import com.sme.erp.inventory.dto.StockMovementPageDTO;
import com.sme.erp.inventory.dto.StockPageDTO;

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

    StockDTO transferOut(Long productId, Long warehouseId, BigDecimal qty, String referenceNo);

    StockDTO transferIn(Long productId, Long warehouseId, BigDecimal qty, String referenceNo);
    
    StockDTO adjustStock(Long productId, Long warehouseId, BigDecimal qty, String reason);

    StockDTO getStock(Long productId, Long warehouseId);
    
    List<StockDTO> getAllStock(); 

    List<StockMovementDTO> getAllMovements();

    StockPageDTO searchStock(String keyword, Long warehouseId, Long categoryId, Boolean lowStockOnly,
                             int page, int size, String sort, String direction);

    StockMovementPageDTO searchMovements(String keyword, Long productId, Long warehouseId, MovementType movementType,
                                         String referenceType, LocalDate fromDate, LocalDate toDate,
                                         int page, int size, String sort, String direction);

    StockCardDTO getStockCard(Long productId, Long warehouseId);
}
