package com.sme.erp.reports.dto;

import com.sme.erp.sales.enums.SalesReturnStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SalesReturnRowDTO(
        String returnNo,
        String customer,
        String invoiceNo,
        LocalDateTime date,
        BigDecimal quantity,
        BigDecimal amount,
        String status) {

    public SalesReturnRowDTO(String returnNo, String customer, String invoiceNo, LocalDateTime date,
                             BigDecimal quantity, BigDecimal amount, SalesReturnStatus status) {
        this(returnNo, customer, invoiceNo, date, quantity, amount, status == null ? "" : status.name());
    }
}
