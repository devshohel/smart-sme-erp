package com.sme.erp.supplier.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SupplierAgingReportDTO {
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal totalDue;
    private List<SupplierAgingRowDTO> rows;

    public SupplierAgingReportDTO(LocalDate fromDate, LocalDate toDate, BigDecimal totalDue, List<SupplierAgingRowDTO> rows) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.totalDue = totalDue;
        this.rows = rows;
    }

    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public BigDecimal getTotalDue() { return totalDue; }
    public List<SupplierAgingRowDTO> getRows() { return rows; }
}
