package com.sme.erp.inventory.entity;

import com.sme.erp.product.entity.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "stock_adjustments")
public class StockAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(precision = 15, scale = 2)
    private BigDecimal quantity;

    private String reason; // DAMAGE / LOSS / CORRECTION

    private String note;

    // getters setters
    public Long getId() { return id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    public void setProductId(Long productId) {
        if (productId == null) {
            this.product = null;
            return;
        }

        if (this.product == null) {
            this.product = new Product();
        }
        this.product.setId(productId);
    }

    public Long getWarehouseId() {
        return warehouse != null ? warehouse.getId() : null;
    }

    public void setWarehouseId(Long warehouseId) {
        if (warehouseId == null) {
            this.warehouse = null;
            return;
        }

        if (this.warehouse == null) {
            this.warehouse = new Warehouse();
        }
        this.warehouse.setId(warehouseId);
    }

    public BigDecimal  getQuantity() { return quantity; }
    public void setQuantity(BigDecimal  quantity) { this.quantity = quantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
