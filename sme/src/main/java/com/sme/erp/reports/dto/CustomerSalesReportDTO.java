package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public record CustomerSalesReportDTO(
        Long totalCustomers,
        BigDecimal totalSales,
        BigDecimal totalPaid,
        BigDecimal totalDue,
        List<CustomerSalesRowDTO> rows) {
}
