package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public class PurchaseReportDTO {
    private BigDecimal totalPurchase;
    private BigDecimal totalPaid;
    private BigDecimal totalDue;
    private Long totalOrders;
    private BigDecimal returnAmount;
    private BigDecimal netPurchase;
    private List<PurchaseReportRowDTO> rows;

    public PurchaseReportDTO(BigDecimal totalPurchase, BigDecimal totalPaid, BigDecimal totalDue,
                             Long totalOrders, BigDecimal returnAmount, BigDecimal netPurchase,
                             List<PurchaseReportRowDTO> rows) {
        this.totalPurchase = totalPurchase;
        this.totalPaid = totalPaid;
        this.totalDue = totalDue;
        this.totalOrders = totalOrders;
        this.returnAmount = returnAmount;
        this.netPurchase = netPurchase;
        this.rows = rows;
    }

    public BigDecimal getTotalPurchase() { return totalPurchase; }
    public BigDecimal getTotalPaid() { return totalPaid; }
    public BigDecimal getTotalDue() { return totalDue; }
    public Long getTotalOrders() { return totalOrders; }
    public BigDecimal getReturnAmount() { return returnAmount; }
    public BigDecimal getNetPurchase() { return netPurchase; }
    public List<PurchaseReportRowDTO> getRows() { return rows; }
}
