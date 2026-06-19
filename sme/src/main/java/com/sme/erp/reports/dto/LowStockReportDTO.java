package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public record LowStockReportDTO(
        Long totalLowStockItems,
        BigDecimal totalShortageQty,
        List<LowStockRowDTO> rows) {
}
