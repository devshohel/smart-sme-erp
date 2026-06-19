package com.sme.erp.reports.dto;

import java.math.BigDecimal;

public record WarehouseStockValuationRowDTO(
        String warehouse,
        Long productCount,
        BigDecimal totalQty,
        BigDecimal stockValue) {
}
