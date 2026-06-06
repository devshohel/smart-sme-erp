package com.sme.erp.reports.dto;

import java.math.BigDecimal;

public class CustomerDueReportRowDTO {
    private String customer;
    private BigDecimal totalSales;
    private BigDecimal paid;
    private BigDecimal due;

    public CustomerDueReportRowDTO(String customer, BigDecimal totalSales, BigDecimal paid, BigDecimal due) {
        this.customer = customer;
        this.totalSales = totalSales;
        this.paid = paid;
        this.due = due;
    }

    public String getCustomer() { return customer; }
    public BigDecimal getTotalSales() { return totalSales; }
    public BigDecimal getPaid() { return paid; }
    public BigDecimal getDue() { return due; }
}
