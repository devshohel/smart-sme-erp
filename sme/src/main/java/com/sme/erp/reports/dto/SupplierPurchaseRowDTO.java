package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SupplierPurchaseRowDTO(
        Long supplierId,
        String supplier,
        Long purchaseCount,
        BigDecimal totalPurchase,
        BigDecimal paidAmount,
        BigDecimal dueAmount,
        LocalDateTime lastPurchaseDate) {
}
