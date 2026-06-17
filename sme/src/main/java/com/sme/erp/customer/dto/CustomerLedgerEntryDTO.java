package com.sme.erp.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerLedgerEntryDTO {
    private LocalDate date;
    private String referenceType;
    private String referenceNo;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal runningBalance;

    public CustomerLedgerEntryDTO() {
    }

    public CustomerLedgerEntryDTO(LocalDate date, String referenceType, String referenceNo, String description,
                                  BigDecimal debit, BigDecimal credit, BigDecimal runningBalance) {
        this.date = date;
        this.referenceType = referenceType;
        this.referenceNo = referenceNo;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
        this.runningBalance = runningBalance;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getDebit() { return debit; }
    public void setDebit(BigDecimal debit) { this.debit = debit; }

    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }

    public BigDecimal getRunningBalance() { return runningBalance; }
    public void setRunningBalance(BigDecimal runningBalance) { this.runningBalance = runningBalance; }
}
