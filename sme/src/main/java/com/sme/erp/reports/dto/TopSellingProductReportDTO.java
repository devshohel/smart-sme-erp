package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public record TopSellingProductReportDTO(
        BigDecimal totalQuantitySold,
        BigDecimal totalGrossSales,
        BigDecimal totalReturnQty,
        BigDecimal totalNetQty,
        List<TopSellingProductRowDTO> rows) {
}
