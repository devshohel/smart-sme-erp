package com.sme.erp.inventory.entity;

import com.sme.erp.enums.MovementType;

import com.sme.erp.product.entity.Product;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "stock_movements", indexes = {
    @Index(name = "idx_movement_product", columnList = "product_id"),
    @Index(name = "idx_movement_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_movement_code", columnList = "movement_code")
})
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movement_code", unique = true, nullable = false)
    private String movementCode; // MOV-1714923456

    // 🔗 Product Relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 🔗 Warehouse Relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType; // IN / OUT

    @Column(precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(precision = 15, scale = 2)
    private BigDecimal unitCost; // kenar somoy dam koto chilo (need for Profit/Loss)

    private String referenceType; // PURCHASE, SALE, ADJUSTMENT, TRANSFER
    private String referenceNo;   // Invoice No or others referance id

    private String batchNo;       // 
    private LocalDate expiryDate; // expaired date of product

    private String note;          // more info

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 🔄 Lifecycle Hooks
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        // auto movement code generation ligic
        if (this.movementCode == null) {
            this.movementCode = "MOV-" + System.currentTimeMillis();
        }
    }

    // Default Constructor
    public StockMovement() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMovementCode() { return movementCode; }
    public void setMovementCode(String movementCode) { this.movementCode = movementCode; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public MovementType getMovementType() { return movementType; }
    public void setMovementType(MovementType movementType) { this.movementType = movementType; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal  getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal  unitCost) { this.unitCost = unitCost; }

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
}