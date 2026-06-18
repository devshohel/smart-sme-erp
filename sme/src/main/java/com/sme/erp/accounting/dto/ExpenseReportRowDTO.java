package com.sme.erp.accounting.dto;

import java.math.BigDecimal;

public class ExpenseReportRowDTO {
    private String label;
    private BigDecimal netAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal grossAmount = BigDecimal.ZERO;
    private long count;

    public ExpenseReportRowDTO() {}

    public ExpenseReportRowDTO(String label, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal grossAmount, long count) {
        this.label = label;
        this.netAmount = netAmount;
        this.taxAmount = taxAmount;
        this.grossAmount = grossAmount;
        this.count = count;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
