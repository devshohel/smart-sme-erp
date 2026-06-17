package com.sme.erp.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CustomerLedgerDTO {
    private CustomerDTO customer;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private List<CustomerLedgerEntryDTO> entries;

    public CustomerLedgerDTO(CustomerDTO customer, LocalDate fromDate, LocalDate toDate,
                             BigDecimal openingBalance, BigDecimal closingBalance,
                             List<CustomerLedgerEntryDTO> entries) {
        this.customer = customer;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.entries = entries;
    }

    public CustomerDTO getCustomer() { return customer; }
    public void setCustomer(CustomerDTO customer) { this.customer = customer; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public BigDecimal getClosingBalance() { return closingBalance; }
    public void setClosingBalance(BigDecimal closingBalance) { this.closingBalance = closingBalance; }

    public List<CustomerLedgerEntryDTO> getEntries() { return entries; }
    public void setEntries(List<CustomerLedgerEntryDTO> entries) { this.entries = entries; }
}
