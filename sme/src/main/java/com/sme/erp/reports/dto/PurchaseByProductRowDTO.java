package com.sme.erp.reports.dto;

import java.math.BigDecimal;

public record PurchaseByProductRowDTO(
        Long productId,
        String product,
        String sku,
        BigDecimal quantityPurchased,
        BigDecimal grossPurchase,
        BigDecimal returnQty,
        BigDecimal netQty) {
}
