package com.sme.erp.supplier.dto;

import java.math.BigDecimal;

public class ApReconciliationSummaryDTO {
    private BigDecimal totalPurchaseDue;
    private BigDecimal totalSupplierAdvance;
    private BigDecimal totalGlAccountsPayable;
    private BigDecimal totalVariance;
    private BigDecimal netSupplierExposure;

    public ApReconciliationSummaryDTO(BigDecimal totalPurchaseDue, BigDecimal totalSupplierAdvance,
                                      BigDecimal totalGlAccountsPayable, BigDecimal totalVariance,
                                      BigDecimal netSupplierExposure) {
        this.totalPurchaseDue = totalPurchaseDue;
        this.totalSupplierAdvance = totalSupplierAdvance;
        this.totalGlAccountsPayable = totalGlAccountsPayable;
        this.totalVariance = totalVariance;
        this.netSupplierExposure = netSupplierExposure;
    }

    public BigDecimal getTotalPurchaseDue() { return totalPurchaseDue; }
    public BigDecimal getTotalSupplierAdvance() { return totalSupplierAdvance; }
    public BigDecimal getTotalGlAccountsPayable() { return totalGlAccountsPayable; }
    public BigDecimal getTotalVariance() { return totalVariance; }
    public BigDecimal getNetSupplierExposure() { return netSupplierExposure; }
}
