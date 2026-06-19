package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerSalesRowDTO(
        Long customerId,
        String customer,
        Long invoiceCount,
        BigDecimal totalSales,
        BigDecimal paidAmount,
        BigDecimal dueAmount,
        LocalDateTime lastSaleDate) {
}
