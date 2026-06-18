package com.sme.erp.supplier.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SupplierStatementRowDTO {
    private LocalDate date;
    private String referenceType;
    private String referenceNo;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal runningBalance;
    private BigDecimal advanceAmount;
    private String status;

    public SupplierStatementRowDTO() {
    }

    public SupplierStatementRowDTO(LocalDate date, String referenceType, String referenceNo, String description,
                                   BigDecimal debit, BigDecimal credit, BigDecimal runningBalance,
                                   BigDecimal advanceAmount, String status) {
        this.date = date;
        this.referenceType = referenceType;
        this.referenceNo = referenceNo;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
        this.runningBalance = runningBalance;
        this.advanceAmount = advanceAmount;
        this.status = status;
    }

    public LocalDate getDate() { return date; }
    public String getReferenceType() { return referenceType; }
    public String getReferenceNo() { return referenceNo; }
    public String getDescription() { return description; }
    public BigDecimal getDebit() { return debit; }
    public BigDecimal getCredit() { return credit; }
    public BigDecimal getRunningBalance() { return runningBalance; }
    public BigDecimal getAdvanceAmount() { return advanceAmount; }
    public String getStatus() { return status; }
}
