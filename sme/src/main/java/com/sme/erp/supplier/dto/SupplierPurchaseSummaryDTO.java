package com.sme.erp.supplier.dto;

import com.sme.erp.purchase.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SupplierPurchaseSummaryDTO {
    private String purchaseNumber;
    private LocalDateTime date;
    private BigDecimal netTotal;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
    private PurchaseStatus status;

    public SupplierPurchaseSummaryDTO(String purchaseNumber, LocalDateTime date, BigDecimal netTotal,
                                      BigDecimal paidAmount, BigDecimal dueAmount, PurchaseStatus status) {
        this.purchaseNumber = purchaseNumber;
        this.date = date;
        this.netTotal = netTotal;
        this.paidAmount = paidAmount;
        this.dueAmount = dueAmount;
        this.status = status;
    }

    public String getPurchaseNumber() { return purchaseNumber; }
    public LocalDateTime getDate() { return date; }
    public BigDecimal getNetTotal() { return netTotal; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public BigDecimal getDueAmount() { return dueAmount; }
    public PurchaseStatus getStatus() { return status; }
}
