package com.sme.erp.reports.dto;

import java.math.BigDecimal;

public class SupplierDueReportRowDTO {
    private String supplier;
    private BigDecimal totalPurchase;
    private BigDecimal paid;
    private BigDecimal due;

    public SupplierDueReportRowDTO(String supplier, BigDecimal totalPurchase, BigDecimal paid, BigDecimal due) {
        this.supplier = supplier;
        this.totalPurchase = totalPurchase;
        this.paid = paid;
        this.due = due;
    }

    public String getSupplier() { return supplier; }
    public BigDecimal getTotalPurchase() { return totalPurchase; }
    public BigDecimal getPaid() { return paid; }
    public BigDecimal getDue() { return due; }
}
