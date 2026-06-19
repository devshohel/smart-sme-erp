package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public class SalesReportDTO {
    private BigDecimal totalSales;
    private BigDecimal totalPaid;
    private BigDecimal totalDue;
    private Long totalInvoices;
    private BigDecimal returnAmount;
    private BigDecimal netSales;
    private List<SalesReportRowDTO> rows;

    public SalesReportDTO(BigDecimal totalSales, BigDecimal totalPaid, BigDecimal totalDue,
                          Long totalInvoices, BigDecimal returnAmount, BigDecimal netSales,
                          List<SalesReportRowDTO> rows) {
        this.totalSales = totalSales;
        this.totalPaid = totalPaid;
        this.totalDue = totalDue;
        this.totalInvoices = totalInvoices;
        this.returnAmount = returnAmount;
        this.netSales = netSales;
        this.rows = rows;
    }

    public BigDecimal getTotalSales() { return totalSales; }
    public BigDecimal getTotalPaid() { return totalPaid; }
    public BigDecimal getTotalDue() { return totalDue; }
    public Long getTotalInvoices() { return totalInvoices; }
    public BigDecimal getReturnAmount() { return returnAmount; }
    public BigDecimal getNetSales() { return netSales; }
    public List<SalesReportRowDTO> getRows() { return rows; }
}
