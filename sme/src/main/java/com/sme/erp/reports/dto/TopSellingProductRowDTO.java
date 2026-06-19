package com.sme.erp.reports.dto;

import java.math.BigDecimal;

public record TopSellingProductRowDTO(
        Long productId,
        String product,
        String sku,
        BigDecimal quantitySold,
        BigDecimal grossSales,
        BigDecimal returnQty,
        BigDecimal netQty) {

    public TopSellingProductRowDTO(Long productId,
                                   String product,
                                   String sku,
                                   BigDecimal quantitySold,
                                   BigDecimal grossSales,
                                   Number returnQty,
                                   BigDecimal netQty) {
        this(
                productId,
                product,
                sku,
                quantitySold,
                grossSales,
                returnQty == null ? BigDecimal.ZERO : BigDecimal.valueOf(returnQty.doubleValue()),
                netQty);
    }
}
