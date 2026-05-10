package com.sme.erp.inventory.entity;

import com.sme.erp.product.entity.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "warehouse_id"})
        })
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

    private Integer reorderLevel = 0;

    private LocalDateTime lastUpdatedAt;

    @PrePersist
    @PreUpdate
    public void updateTime() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // getters setters
    public Long getId() { return id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
}