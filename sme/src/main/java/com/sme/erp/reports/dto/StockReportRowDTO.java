package com.sme.erp.reports.dto;

import java.math.BigDecimal;

public class StockReportRowDTO {
    private String product;
    private String warehouse;
    private BigDecimal quantity;
    private BigDecimal reorderLevel;
    private BigDecimal stockValue;

    public StockReportRowDTO(String product, String warehouse, BigDecimal quantity,
                             Number reorderLevel, BigDecimal stockValue) {
        this.product = product;
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel == null ? BigDecimal.ZERO : BigDecimal.valueOf(reorderLevel.longValue());
        this.stockValue = stockValue;
    }

    public String getProduct() { return product; }
    public String getWarehouse() { return warehouse; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getReorderLevel() { return reorderLevel; }
    public BigDecimal getStockValue() { return stockValue; }
}
