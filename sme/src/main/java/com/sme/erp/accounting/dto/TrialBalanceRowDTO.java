package com.sme.erp.accounting.dto;

import com.sme.erp.accounting.enums.AccountType;

import java.math.BigDecimal;

public class TrialBalanceRowDTO {
    private String accountCode;
    private String accountName;
    private AccountType accountType;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal balance;

    public TrialBalanceRowDTO(String accountCode, String accountName, AccountType accountType, BigDecimal totalDebit, BigDecimal totalCredit, BigDecimal balance) {
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.totalDebit = totalDebit;
        this.totalCredit = totalCredit;
        this.balance = balance;
    }

    public String getAccountCode() { return accountCode; }
    public String getAccountName() { return accountName; }
    public AccountType getAccountType() { return accountType; }
    public BigDecimal getTotalDebit() { return totalDebit; }
    public BigDecimal getTotalCredit() { return totalCredit; }
    public BigDecimal getBalance() { return balance; }
}
