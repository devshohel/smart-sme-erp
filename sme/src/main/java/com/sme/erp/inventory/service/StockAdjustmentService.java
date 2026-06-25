package com.sme.erp.inventory.service;

import com.sme.erp.inventory.dto.StockAdjustmentDTO;

import java.math.BigDecimal;
import java.util.List;

public interface StockAdjustmentService {
    List<StockAdjustmentDTO> getAll();
    StockAdjustmentDTO getById(Long id);
    StockAdjustmentDTO create(Long productId, Long warehouseId, BigDecimal quantity, String reason);
    StockAdjustmentDTO approve(Long id);
    StockAdjustmentDTO post(Long id);
    StockAdjustmentDTO cancel(Long id);
    StockAdjustmentDTO reverse(Long id, String reversalReason);
}
