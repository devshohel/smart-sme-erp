package com.sme.erp.supplier.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SupplierReturnSummaryDTO {
    private String returnNumber;
    private LocalDateTime date;
    private BigDecimal amount;
    private String status;

    public SupplierReturnSummaryDTO(String returnNumber, LocalDateTime date, BigDecimal amount, String status) {
        this.returnNumber = returnNumber;
        this.date = date;
        this.amount = amount;
        this.status = status;
    }

    public String getReturnNumber() { return returnNumber; }
    public LocalDateTime getDate() { return date; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
}
