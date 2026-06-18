package com.sme.erp.supplier.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SupplierStatementDTO {
    private SupplierDTO supplier;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal openingBalance;
    private BigDecimal closingPayableBalance;
    private BigDecimal supplierAdvanceBalance;
    private BigDecimal netSupplierPosition;
    private List<SupplierStatementRowDTO> rows;

    public SupplierStatementDTO(SupplierDTO supplier, LocalDate fromDate, LocalDate toDate,
                                BigDecimal openingBalance, BigDecimal closingPayableBalance,
                                BigDecimal supplierAdvanceBalance, BigDecimal netSupplierPosition,
                                List<SupplierStatementRowDTO> rows) {
        this.supplier = supplier;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.openingBalance = openingBalance;
        this.closingPayableBalance = closingPayableBalance;
        this.supplierAdvanceBalance = supplierAdvanceBalance;
        this.netSupplierPosition = netSupplierPosition;
        this.rows = rows;
    }

    public SupplierDTO getSupplier() { return supplier; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public BigDecimal getClosingPayableBalance() { return closingPayableBalance; }
    public BigDecimal getSupplierAdvanceBalance() { return supplierAdvanceBalance; }
    public BigDecimal getNetSupplierPosition() { return netSupplierPosition; }
    public List<SupplierStatementRowDTO> getRows() { return rows; }
}
