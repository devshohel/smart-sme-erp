package com.sme.erp.purchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PurchaseReceiveItemDTO {
    private Long id;

    @NotNull(message = "Product id is required")
    @Positive(message = "Product id must be positive")
    private Long productId;

    private String productName;
    private BigDecimal orderedQty;

    @NotNull(message = "Received quantity is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Received quantity must be positive")
    private BigDecimal receivedQty;

    private BigDecimal remainingQty;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getOrderedQty() { return orderedQty; }
    public void setOrderedQty(BigDecimal orderedQty) { this.orderedQty = orderedQty; }
    public BigDecimal getReceivedQty() { return receivedQty; }
    public void setReceivedQty(BigDecimal receivedQty) { this.receivedQty = receivedQty; }
    public BigDecimal getRemainingQty() { return remainingQty; }
    public void setRemainingQty(BigDecimal remainingQty) { this.remainingQty = remainingQty; }
}
