package com.sme.erp.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class StockTransferItemDTO {
    private Long id;

    @NotNull
    @Positive
    private Long productId;

    private String productName;
    private String sku;

    @NotNull
    @Positive
    private BigDecimal quantity;

    private String remarks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
