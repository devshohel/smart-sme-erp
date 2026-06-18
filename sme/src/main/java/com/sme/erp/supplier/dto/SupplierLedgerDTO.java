package com.sme.erp.supplier.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SupplierLedgerDTO {
    private SupplierDTO supplier;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private List<SupplierLedgerEntryDTO> entries;

    public SupplierLedgerDTO(SupplierDTO supplier, LocalDate fromDate, LocalDate toDate,
                             BigDecimal openingBalance, BigDecimal closingBalance,
                             List<SupplierLedgerEntryDTO> entries) {
        this.supplier = supplier;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.entries = entries;
    }

    public SupplierDTO getSupplier() { return supplier; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public BigDecimal getClosingBalance() { return closingBalance; }
    public List<SupplierLedgerEntryDTO> getEntries() { return entries; }
}
