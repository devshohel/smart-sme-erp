package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.sme.erp.purchase.enums.PurchaseStatus;

public class PurchaseReportRowDTO {
    private String poNo;
    private String supplier;
    private LocalDateTime date;
    private BigDecimal amount;
    private BigDecimal paid;
    private BigDecimal due;
    private String status;

    public PurchaseReportRowDTO(String poNo, String supplier, LocalDateTime date, BigDecimal amount,
                                BigDecimal paid, BigDecimal due, PurchaseStatus status) {
        this.poNo = poNo;
        this.supplier = supplier;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
        this.due = due;
        this.status = status == null ? "" : status.name();
    }

    public String getPoNo() { return poNo; }
    public String getSupplier() { return supplier; }
    public LocalDateTime getDate() { return date; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getPaid() { return paid; }
    public BigDecimal getDue() { return due; }
    public String getStatus() { return status; }
}
