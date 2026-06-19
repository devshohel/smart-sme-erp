package com.sme.erp.reports.dto;

import com.sme.erp.sales.enums.SalesInvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesReportRowDTO {
    private String invoiceNo;
    private String customer;
    private String warehouse;
    private String status;
    private LocalDateTime date;
    private BigDecimal quantity;
    private BigDecimal amount;
    private BigDecimal paid;
    private BigDecimal due;

    public SalesReportRowDTO(String invoiceNo, String customer, String warehouse, String status,
                             LocalDateTime date, BigDecimal quantity, BigDecimal amount,
                             BigDecimal paid, BigDecimal due) {
        this.invoiceNo = invoiceNo;
        this.customer = customer;
        this.warehouse = warehouse;
        this.status = status;
        this.date = date;
        this.quantity = quantity;
        this.amount = amount;
        this.paid = paid;
        this.due = due;
    }

    public SalesReportRowDTO(String invoiceNo, String customer, String warehouse, SalesInvoiceStatus status,
                             LocalDateTime date, BigDecimal quantity, BigDecimal amount,
                             BigDecimal paid, BigDecimal due) {
        this(invoiceNo, customer, warehouse, status == null ? "" : status.name(), date, quantity, amount, paid, due);
    }

    public String getInvoiceNo() { return invoiceNo; }
    public String getCustomer() { return customer; }
    public String getWarehouse() { return warehouse; }
    public String getStatus() { return status; }
    public LocalDateTime getDate() { return date; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getPaid() { return paid; }
    public BigDecimal getDue() { return due; }
}
