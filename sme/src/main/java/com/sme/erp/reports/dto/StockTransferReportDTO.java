package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public record StockTransferReportDTO(
        Long totalTransfers,
        Long totalItems,
        BigDecimal totalQuantity,
        List<StockTransferRowDTO> rows) {
}
