package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalesReturnReportDTO(
        Long returnCount,
        BigDecimal totalReturnAmount,
        List<SalesReturnRowDTO> rows) {
}
