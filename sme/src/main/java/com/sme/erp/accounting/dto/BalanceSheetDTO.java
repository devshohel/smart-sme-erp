package com.sme.erp.accounting.dto;

import java.math.BigDecimal;

public class BalanceSheetDTO {
    private BigDecimal cash;
    private BigDecimal bank;
    private BigDecimal accountsReceivable;
    private BigDecimal inventoryValue;
    private BigDecimal totalAssets;
    private BigDecimal accountsPayable;
    private BigDecimal totalLiabilities;
    private BigDecimal ownerEquity;
    private BigDecimal retainedEarnings;
    private BigDecimal totalEquity;
    private BigDecimal liabilitiesAndEquity;

    public BalanceSheetDTO(BigDecimal cash, BigDecimal bank, BigDecimal accountsReceivable, BigDecimal inventoryValue,
                           BigDecimal accountsPayable, BigDecimal ownerEquity, BigDecimal retainedEarnings) {
        this.cash = cash;
        this.bank = bank;
        this.accountsReceivable = accountsReceivable;
        this.inventoryValue = inventoryValue;
        this.totalAssets = cash.add(bank).add(accountsReceivable).add(inventoryValue);
        this.accountsPayable = accountsPayable;
        this.totalLiabilities = accountsPayable;
        this.ownerEquity = ownerEquity;
        this.retainedEarnings = retainedEarnings;
        this.totalEquity = ownerEquity.add(retainedEarnings);
        this.liabilitiesAndEquity = this.totalLiabilities.add(this.totalEquity);
    }

    public BigDecimal getCash() { return cash; }
    public BigDecimal getBank() { return bank; }
    public BigDecimal getAccountsReceivable() { return accountsReceivable; }
    public BigDecimal getInventoryValue() { return inventoryValue; }
    public BigDecimal getTotalAssets() { return totalAssets; }
    public BigDecimal getAccountsPayable() { return accountsPayable; }
    public BigDecimal getTotalLiabilities() { return totalLiabilities; }
    public BigDecimal getOwnerEquity() { return ownerEquity; }
    public BigDecimal getRetainedEarnings() { return retainedEarnings; }
    public BigDecimal getTotalEquity() { return totalEquity; }
    public BigDecimal getLiabilitiesAndEquity() { return liabilitiesAndEquity; }
}
