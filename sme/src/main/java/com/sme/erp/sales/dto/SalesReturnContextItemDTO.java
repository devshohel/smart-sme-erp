package com.sme.erp.sales.dto;

import java.math.BigDecimal;

public class SalesReturnContextItemDTO {
    private Long invoiceItemId;
    private Long productId;
    private String productName;
    private BigDecimal soldQuantity;
    private BigDecimal returnedQuantity;
    private BigDecimal remainingQuantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal tax;

    public Long getInvoiceItemId() { return invoiceItemId; }
    public void setInvoiceItemId(Long invoiceItemId) { this.invoiceItemId = invoiceItemId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(BigDecimal soldQuantity) { this.soldQuantity = soldQuantity; }
    public BigDecimal getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(BigDecimal returnedQuantity) { this.returnedQuantity = returnedQuantity; }
    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(BigDecimal remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
}
