package com.sme.erp.supplier.dto;

import java.math.BigDecimal;

public class ApReconciliationBreakdownDTO {
    private BigDecimal purchaseGross;
    private BigDecimal purchaseReturns;
    private BigDecimal allocatedPayments;
    private BigDecimal paymentReversals;
    private BigDecimal supplierAdvance;
    private BigDecimal manualApAdjustments;

    public ApReconciliationBreakdownDTO(BigDecimal purchaseGross, BigDecimal purchaseReturns,
                                        BigDecimal allocatedPayments, BigDecimal paymentReversals,
                                        BigDecimal supplierAdvance, BigDecimal manualApAdjustments) {
        this.purchaseGross = purchaseGross;
        this.purchaseReturns = purchaseReturns;
        this.allocatedPayments = allocatedPayments;
        this.paymentReversals = paymentReversals;
        this.supplierAdvance = supplierAdvance;
        this.manualApAdjustments = manualApAdjustments;
    }

    public BigDecimal getPurchaseGross() { return purchaseGross; }
    public BigDecimal getPurchaseReturns() { return purchaseReturns; }
    public BigDecimal getAllocatedPayments() { return allocatedPayments; }
    public BigDecimal getPaymentReversals() { return paymentReversals; }
    public BigDecimal getSupplierAdvance() { return supplierAdvance; }
    public BigDecimal getManualApAdjustments() { return manualApAdjustments; }
}
