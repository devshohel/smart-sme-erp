package com.sme.erp.inventory.dto;

import com.sme.erp.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StockMovementDTO {

    private Long id;
    private String movementCode;
    private ProductRefDTO product;
    private WarehouseRefDTO warehouse;
    private Long productId;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private MovementType movementType;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private String referenceType;
    private String referenceNo;
    private String batchNo;
    private LocalDate expiryDate;
    private String note;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMovementCode() { return movementCode; }
    public void setMovementCode(String movementCode) { this.movementCode = movementCode; }

    public ProductRefDTO getProduct() { return product; }
    public void setProduct(ProductRefDTO product) { this.product = product; }

    public WarehouseRefDTO getWarehouse() { return warehouse; }
    public void setWarehouse(WarehouseRefDTO warehouse) { this.warehouse = warehouse; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public MovementType getMovementType() { return movementType; }
    public void setMovementType(MovementType movementType) { this.movementType = movementType; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class ProductRefDTO {
        private Long id;
        private String productName;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
    }

    public static class WarehouseRefDTO {
        private Long id;
        private String warehouseCode;
        private String warehouseName;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getWarehouseCode() { return warehouseCode; }
        public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }

        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    }
}
