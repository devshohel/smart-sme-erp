package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public class PurchaseReportDTO {
    private BigDecimal totalPurchase;
    private BigDecimal totalPaid;
    private BigDecimal totalDue;
    private Long totalOrders;
    private List<PurchaseReportRowDTO> rows;

    public PurchaseReportDTO(BigDecimal totalPurchase, BigDecimal totalPaid, BigDecimal totalDue,
                             Long totalOrders, List<PurchaseReportRowDTO> rows) {
        this.totalPurchase = totalPurchase;
        this.totalPaid = totalPaid;
        this.totalDue = totalDue;
        this.totalOrders = totalOrders;
        this.rows = rows;
    }

    public BigDecimal getTotalPurchase() { return totalPurchase; }
    public BigDecimal getTotalPaid() { return totalPaid; }
    public BigDecimal getTotalDue() { return totalDue; }
    public Long getTotalOrders() { return totalOrders; }
    public List<PurchaseReportRowDTO> getRows() { return rows; }
}
