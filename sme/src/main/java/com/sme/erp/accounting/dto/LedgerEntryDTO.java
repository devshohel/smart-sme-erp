package com.sme.erp.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LedgerEntryDTO {
    private LocalDate date;
    private String account;
    private String referenceNo;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal balance;

    public LedgerEntryDTO(LocalDate date, String account, String referenceNo, String description, BigDecimal debit, BigDecimal credit, BigDecimal balance) {
        this.date = date;
        this.account = account;
        this.referenceNo = referenceNo;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
        this.balance = balance;
    }

    public LocalDate getDate() { return date; }
    public String getAccount() { return account; }
    public String getReferenceNo() { return referenceNo; }
    public String getDescription() { return description; }
    public BigDecimal getDebit() { return debit; }
    public BigDecimal getCredit() { return credit; }
    public BigDecimal getBalance() { return balance; }
}
