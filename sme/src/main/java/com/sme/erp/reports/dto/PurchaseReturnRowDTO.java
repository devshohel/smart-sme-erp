package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseReturnRowDTO(
        String returnNo,
        String supplier,
        String purchaseNo,
        LocalDateTime date,
        BigDecimal amount,
        String status) {
}
