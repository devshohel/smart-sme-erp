package com.sme.erp.reports.dto;

import com.sme.erp.enums.Status;

import java.math.BigDecimal;

public class StockReportRowDTO {
    private String product;
    private String sku;
    private String category;
    private String brand;
    private String warehouse;
    private BigDecimal quantity;
    private BigDecimal reorderLevel;
    private String status;
    private BigDecimal stockValue;

    public StockReportRowDTO(String product, String sku, String category, String brand,
                             String warehouse, BigDecimal quantity, Number reorderLevel,
                             String status, BigDecimal stockValue) {
        this.product = product;
        this.sku = sku;
        this.category = category;
        this.brand = brand;
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel == null ? BigDecimal.ZERO : BigDecimal.valueOf(reorderLevel.longValue());
        this.status = status;
        this.stockValue = stockValue;
    }

    public StockReportRowDTO(String product, String sku, String category, String brand,
                             String warehouse, BigDecimal quantity, Number reorderLevel,
                             Status status, BigDecimal stockValue) {
        this(product, sku, category, brand, warehouse, quantity, reorderLevel,
                status == null ? "" : status.name(), stockValue);
    }

    public String getProduct() { return product; }
    public String getSku() { return sku; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public String getWarehouse() { return warehouse; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getReorderLevel() { return reorderLevel; }
    public String getStatus() { return status; }
    public BigDecimal getStockValue() { return stockValue; }
}
