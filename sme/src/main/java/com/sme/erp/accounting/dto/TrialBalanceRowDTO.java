package com.sme.erp.accounting.dto;

import com.sme.erp.accounting.enums.AccountType;

import java.math.BigDecimal;

public class TrialBalanceRowDTO {
    private String accountCode;
    private String accountName;
    private AccountType accountType;
    private BigDecimal debitBalance;
    private BigDecimal creditBalance;

    public TrialBalanceRowDTO(String accountCode, String accountName, AccountType accountType, BigDecimal debitBalance, BigDecimal creditBalance) {
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.debitBalance = debitBalance;
        this.creditBalance = creditBalance;
    }

    public String getAccountCode() { return accountCode; }
    public String getAccountName() { return accountName; }
    public AccountType getAccountType() { return accountType; }
    public BigDecimal getDebitBalance() { return debitBalance; }
    public BigDecimal getCreditBalance() { return creditBalance; }
}
