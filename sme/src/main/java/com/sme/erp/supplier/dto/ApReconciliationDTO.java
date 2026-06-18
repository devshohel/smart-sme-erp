package com.sme.erp.supplier.dto;

import java.util.List;

public class ApReconciliationDTO {
    private ApReconciliationSummaryDTO summary;
    private List<ApReconciliationRowDTO> rows;
    private ApReconciliationBreakdownDTO breakdown;

    public ApReconciliationDTO(ApReconciliationSummaryDTO summary, List<ApReconciliationRowDTO> rows,
                               ApReconciliationBreakdownDTO breakdown) {
        this.summary = summary;
        this.rows = rows;
        this.breakdown = breakdown;
    }

    public ApReconciliationSummaryDTO getSummary() { return summary; }
    public List<ApReconciliationRowDTO> getRows() { return rows; }
    public ApReconciliationBreakdownDTO getBreakdown() { return breakdown; }
}
