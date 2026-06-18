package com.sme.erp.supplier.dto;

import java.math.BigDecimal;

public class ApReconciliationRowDTO {
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private BigDecimal purchaseDue;
    private BigDecimal supplierAdvance;
    private BigDecimal glAccountsPayable;
    private BigDecimal variance;
    private BigDecimal netExposure;
    private String status;

    public ApReconciliationRowDTO(Long supplierId, String supplierCode, String supplierName,
                                  BigDecimal purchaseDue, BigDecimal supplierAdvance,
                                  BigDecimal glAccountsPayable, BigDecimal variance,
                                  BigDecimal netExposure, String status) {
        this.supplierId = supplierId;
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.purchaseDue = purchaseDue;
        this.supplierAdvance = supplierAdvance;
        this.glAccountsPayable = glAccountsPayable;
        this.variance = variance;
        this.netExposure = netExposure;
        this.status = status;
    }

    public Long getSupplierId() { return supplierId; }
    public String getSupplierCode() { return supplierCode; }
    public String getSupplierName() { return supplierName; }
    public BigDecimal getPurchaseDue() { return purchaseDue; }
    public BigDecimal getSupplierAdvance() { return supplierAdvance; }
    public BigDecimal getGlAccountsPayable() { return glAccountsPayable; }
    public BigDecimal getVariance() { return variance; }
    public BigDecimal getNetExposure() { return netExposure; }
    public String getStatus() { return status; }
}
