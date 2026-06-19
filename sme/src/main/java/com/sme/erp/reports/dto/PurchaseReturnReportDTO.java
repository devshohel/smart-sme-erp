package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseReturnReportDTO(
        Long returnCount,
        BigDecimal totalReturnAmount,
        List<PurchaseReturnRowDTO> rows) {
}
