package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockMovementReportRowDTO {
    private LocalDateTime date;
    private String product;
    private String movementType;
    private BigDecimal quantity;
    private String referenceNo;

    public StockMovementReportRowDTO(LocalDateTime date, String product, String movementType,
                                     BigDecimal quantity, String referenceNo) {
        this.date = date;
        this.product = product;
        this.movementType = movementType;
        this.quantity = quantity;
        this.referenceNo = referenceNo;
    }

    public LocalDateTime getDate() { return date; }
    public String getProduct() { return product; }
    public String getMovementType() { return movementType; }
    public BigDecimal getQuantity() { return quantity; }
    public String getReferenceNo() { return referenceNo; }
}
