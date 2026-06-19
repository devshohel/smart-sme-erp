package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public record WarehouseStockValuationReportDTO(
        Long totalWarehouses,
        BigDecimal totalQuantity,
        BigDecimal totalStockValue,
        List<WarehouseStockValuationRowDTO> rows) {
}
