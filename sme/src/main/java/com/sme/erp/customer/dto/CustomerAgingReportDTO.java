package com.sme.erp.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CustomerAgingReportDTO {
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal totalDue;
    private List<CustomerAgingRowDTO> rows;

    public CustomerAgingReportDTO(LocalDate fromDate, LocalDate toDate, BigDecimal totalDue, List<CustomerAgingRowDTO> rows) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.totalDue = totalDue;
        this.rows = rows;
    }

    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public BigDecimal getTotalDue() { return totalDue; }
    public List<CustomerAgingRowDTO> getRows() { return rows; }
}
