package com.sme.erp.reports.dto;

import com.sme.erp.inventory.enums.StockTransferStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockTransferRowDTO(
        String transferNo,
        String sourceWarehouse,
        String destinationWarehouse,
        String status,
        LocalDate date,
        Long itemCount,
        BigDecimal totalQty) {

    public StockTransferRowDTO(String transferNo,
                               String sourceWarehouse,
                               String destinationWarehouse,
                               StockTransferStatus status,
                               LocalDate date,
                               Long itemCount,
                               BigDecimal totalQty) {
        this(
                transferNo,
                sourceWarehouse,
                destinationWarehouse,
                status == null ? "" : status.name(),
                date,
                itemCount,
                totalQty);
    }
}
