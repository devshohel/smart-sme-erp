package com.sme.erp.reports.dto;

import java.math.BigDecimal;

public record LowStockRowDTO(
        String product,
        String sku,
        String warehouse,
        BigDecimal currentQty,
        BigDecimal reorderLevel,
        BigDecimal shortageQty) {
}
