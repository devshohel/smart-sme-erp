package com.sme.erp.reports.dto;

import com.sme.erp.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockMovementReportRowDTO {
    private LocalDateTime date;
    private String product;
    private String warehouse;
    private String movementType;
    private BigDecimal quantity;
    private BigDecimal quantityBefore;
    private BigDecimal quantityChange;
    private BigDecimal quantityAfter;
    private String referenceNo;

    public StockMovementReportRowDTO(LocalDateTime date, String product, String warehouse,
                                     String movementType, BigDecimal quantity,
                                     BigDecimal quantityBefore, BigDecimal quantityChange,
                                     BigDecimal quantityAfter, String referenceNo) {
        this.date = date;
        this.product = product;
        this.warehouse = warehouse;
        this.movementType = movementType;
        this.quantity = quantity;
        this.quantityBefore = quantityBefore;
        this.quantityChange = quantityChange;
        this.quantityAfter = quantityAfter;
        this.referenceNo = referenceNo;
    }

    public StockMovementReportRowDTO(LocalDateTime date, String product, String warehouse,
                                     MovementType movementType, BigDecimal quantity,
                                     BigDecimal quantityBefore, BigDecimal quantityChange,
                                     BigDecimal quantityAfter, String referenceNo) {
        this(date, product, warehouse, movementType == null ? "" : movementType.name(), quantity,
                quantityBefore, quantityChange, quantityAfter, referenceNo);
    }

    public LocalDateTime getDate() { return date; }
    public String getProduct() { return product; }
    public String getWarehouse() { return warehouse; }
    public String getMovementType() { return movementType; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getQuantityBefore() { return quantityBefore; }
    public BigDecimal getQuantityChange() { return quantityChange; }
    public BigDecimal getQuantityAfter() { return quantityAfter; }
    public String getReferenceNo() { return referenceNo; }
}
