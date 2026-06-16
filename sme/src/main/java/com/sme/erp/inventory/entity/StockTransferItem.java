package com.sme.erp.inventory.entity;

import com.sme.erp.product.entity.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_transfer_items")
public class StockTransferItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private StockTransfer transfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(length = 500)
    private String remarks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StockTransfer getTransfer() { return transfer; }
    public void setTransfer(StockTransfer transfer) { this.transfer = transfer; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
