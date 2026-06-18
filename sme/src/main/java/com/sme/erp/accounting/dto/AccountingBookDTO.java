package com.sme.erp.accounting.dto;

import java.math.BigDecimal;
import java.util.List;

public class AccountingBookDTO {
    private final BigDecimal openingBalance;
    private final BigDecimal closingBalance;
    private final List<BookEntryDTO> rows;

    public AccountingBookDTO(BigDecimal openingBalance, BigDecimal closingBalance, List<BookEntryDTO> rows) {
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.rows = rows;
    }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public BigDecimal getClosingBalance() { return closingBalance; }
    public List<BookEntryDTO> getRows() { return rows; }
}
