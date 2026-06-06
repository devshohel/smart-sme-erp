package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public class SupplierDueReportDTO {
    private BigDecimal totalSupplierDue;
    private Long totalSuppliersWithDue;
    private List<SupplierDueReportRowDTO> rows;

    public SupplierDueReportDTO(BigDecimal totalSupplierDue, Long totalSuppliersWithDue,
                                List<SupplierDueReportRowDTO> rows) {
        this.totalSupplierDue = totalSupplierDue;
        this.totalSuppliersWithDue = totalSuppliersWithDue;
        this.rows = rows;
    }

    public BigDecimal getTotalSupplierDue() { return totalSupplierDue; }
    public Long getTotalSuppliersWithDue() { return totalSuppliersWithDue; }
    public List<SupplierDueReportRowDTO> getRows() { return rows; }
}
