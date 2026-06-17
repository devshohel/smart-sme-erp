package com.sme.erp.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerTransactionDTO {
    private Long id;
    private String documentNo;
    private LocalDateTime date;
    private BigDecimal amount;
    private BigDecimal paid;
    private BigDecimal due;
    private String status;

    public CustomerTransactionDTO(Long id, String documentNo, LocalDateTime date, BigDecimal amount,
                                  BigDecimal paid, BigDecimal due, String status) {
        this.id = id;
        this.documentNo = documentNo;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
        this.due = due;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getDocumentNo() { return documentNo; }
    public LocalDateTime getDate() { return date; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getPaid() { return paid; }
    public BigDecimal getDue() { return due; }
    public String getStatus() { return status; }
}
