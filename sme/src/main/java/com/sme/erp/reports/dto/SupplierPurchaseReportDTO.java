package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public record SupplierPurchaseReportDTO(
        Long totalSuppliers,
        BigDecimal totalPurchase,
        BigDecimal totalPaid,
        BigDecimal totalDue,
        List<SupplierPurchaseRowDTO> rows) {
}
