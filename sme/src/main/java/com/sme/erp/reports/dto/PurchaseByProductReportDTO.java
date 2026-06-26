package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseByProductReportDTO(
        BigDecimal totalQuantityPurchased,
        BigDecimal totalGrossPurchase,
        BigDecimal totalReturnQty,
        BigDecimal totalNetQty,
        List<PurchaseByProductRowDTO> rows) {
}
