package com.sme.erp.product.entity;

import jakarta.persistence.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sme.erp.enums.Status;

@Entity
@Table(name = "uoms", indexes = {
        @Index(name = "idx_uom_code", columnList = "code")
})
public class Uom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String type; // e.g., Weight, Quantity

    @Column(precision = 10, scale = 4)
    private BigDecimal conversionFactor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    // 🕒 Audit Fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🔄 Lifecycle Hooks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor
    public Uom() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) {this.id = id;}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getConversionFactor() { return conversionFactor; }
    public void setConversionFactor(BigDecimal conversionFactor) { this.conversionFactor = conversionFactor; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}