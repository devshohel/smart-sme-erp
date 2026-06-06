package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesReportRowDTO {
    private String invoiceNo;
    private String customer;
    private LocalDateTime date;
    private BigDecimal quantity;
    private BigDecimal amount;
    private BigDecimal paid;
    private BigDecimal due;

    public SalesReportRowDTO(String invoiceNo, String customer, LocalDateTime date, BigDecimal quantity,
                             BigDecimal amount, BigDecimal paid, BigDecimal due) {
        this.invoiceNo = invoiceNo;
        this.customer = customer;
        this.date = date;
        this.quantity = quantity;
        this.amount = amount;
        this.paid = paid;
        this.due = due;
    }

    public String getInvoiceNo() { return invoiceNo; }
    public String getCustomer() { return customer; }
    public LocalDateTime getDate() { return date; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getPaid() { return paid; }
    public BigDecimal getDue() { return due; }
}
