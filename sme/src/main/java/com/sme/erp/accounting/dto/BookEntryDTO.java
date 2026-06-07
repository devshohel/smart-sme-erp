package com.sme.erp.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookEntryDTO {
    private LocalDate date;
    private String reference;
    private String description;
    private BigDecimal moneyIn;
    private BigDecimal moneyOut;
    private BigDecimal balance;

    public BookEntryDTO(LocalDate date, String reference, String description, BigDecimal moneyIn, BigDecimal moneyOut, BigDecimal balance) {
        this.date = date;
        this.reference = reference;
        this.description = description;
        this.moneyIn = moneyIn;
        this.moneyOut = moneyOut;
        this.balance = balance;
    }

    public LocalDate getDate() { return date; }
    public String getReference() { return reference; }
    public String getDescription() { return description; }
    public BigDecimal getMoneyIn() { return moneyIn; }
    public BigDecimal getMoneyOut() { return moneyOut; }
    public BigDecimal getBalance() { return balance; }
}
