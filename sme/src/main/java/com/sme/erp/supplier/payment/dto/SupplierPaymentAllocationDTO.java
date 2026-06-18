package com.sme.erp.supplier.payment.dto;

import java.math.BigDecimal;

public class SupplierPaymentAllocationDTO {
    private Long id;
    private Long purchaseOrderId;
    private String purchaseCode;
    private BigDecimal purchaseDueAmount;
    private BigDecimal allocatedAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public String getPurchaseCode() { return purchaseCode; }
    public void setPurchaseCode(String purchaseCode) { this.purchaseCode = purchaseCode; }
    public BigDecimal getPurchaseDueAmount() { return purchaseDueAmount; }
    public void setPurchaseDueAmount(BigDecimal purchaseDueAmount) { this.purchaseDueAmount = purchaseDueAmount; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
}
