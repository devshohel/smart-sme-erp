package com.sme.erp.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookEntryDTO {
    private LocalDate date;
    private String journalNo;
    private String referenceType;
    private String referenceNo;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal runningBalance;

    public BookEntryDTO(LocalDate date, String journalNo, String referenceType, String referenceNo, String description, BigDecimal debit, BigDecimal credit, BigDecimal runningBalance) {
        this.date = date;
        this.journalNo = journalNo;
        this.referenceType = referenceType;
        this.referenceNo = referenceNo;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
        this.runningBalance = runningBalance;
    }

    public LocalDate getDate() { return date; }
    public String getJournalNo() { return journalNo; }
    public String getReferenceType() { return referenceType; }
    public String getReferenceNo() { return referenceNo; }
    public String getDescription() { return description; }
    public BigDecimal getDebit() { return debit; }
    public BigDecimal getCredit() { return credit; }
    public BigDecimal getRunningBalance() { return runningBalance; }
}
