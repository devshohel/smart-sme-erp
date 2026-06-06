package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public class StockReportDTO {
    private BigDecimal totalStockQuantity;
    private BigDecimal totalStockValue;
    private Long lowStockCount;
    private List<StockReportRowDTO> rows;
    private List<StockMovementReportRowDTO> movements;

    public StockReportDTO(BigDecimal totalStockQuantity, BigDecimal totalStockValue, Long lowStockCount,
                          List<StockReportRowDTO> rows, List<StockMovementReportRowDTO> movements) {
        this.totalStockQuantity = totalStockQuantity;
        this.totalStockValue = totalStockValue;
        this.lowStockCount = lowStockCount;
        this.rows = rows;
        this.movements = movements;
    }

    public BigDecimal getTotalStockQuantity() { return totalStockQuantity; }
    public BigDecimal getTotalStockValue() { return totalStockValue; }
    public Long getLowStockCount() { return lowStockCount; }
    public List<StockReportRowDTO> getRows() { return rows; }
    public List<StockMovementReportRowDTO> getMovements() { return movements; }
}
