package com.sme.erp.supplier.dto;

import com.sme.erp.supplier.payment.dto.SupplierPaymentDTO;

import java.math.BigDecimal;
import java.util.List;

public class SupplierDetailDTO {
    private SupplierDTO supplier;
    private BigDecimal supplierDue;
    private List<SupplierPurchaseSummaryDTO> recentPurchases;
    private List<SupplierReturnSummaryDTO> recentPurchaseReturns;
    private List<SupplierPaymentDTO> recentPayments;

    public SupplierDetailDTO(SupplierDTO supplier, BigDecimal supplierDue,
                             List<SupplierPurchaseSummaryDTO> recentPurchases,
                             List<SupplierReturnSummaryDTO> recentPurchaseReturns,
                             List<SupplierPaymentDTO> recentPayments) {
        this.supplier = supplier;
        this.supplierDue = supplierDue;
        this.recentPurchases = recentPurchases;
        this.recentPurchaseReturns = recentPurchaseReturns;
        this.recentPayments = recentPayments;
    }

    public SupplierDTO getSupplier() { return supplier; }
    public BigDecimal getSupplierDue() { return supplierDue; }
    public List<SupplierPurchaseSummaryDTO> getRecentPurchases() { return recentPurchases; }
    public List<SupplierReturnSummaryDTO> getRecentPurchaseReturns() { return recentPurchaseReturns; }
    public List<SupplierPaymentDTO> getRecentPayments() { return recentPayments; }
}
