package com.sme.erp.purchase.entity;

import com.sme.erp.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "goods_receive_items")
public class GoodsReceiveItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "goods_receive_note_id", nullable = false)
    private GoodsReceiveNote goodsReceiveNote;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "ordered_qty", precision = 15, scale = 2, nullable = false)
    private BigDecimal orderedQty = BigDecimal.ZERO;

    @Column(name = "received_qty", precision = 15, scale = 2, nullable = false)
    private BigDecimal receivedQty = BigDecimal.ZERO;

    @Column(name = "remaining_qty", precision = 15, scale = 2, nullable = false)
    private BigDecimal remainingQty = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public GoodsReceiveNote getGoodsReceiveNote() { return goodsReceiveNote; }
    public void setGoodsReceiveNote(GoodsReceiveNote goodsReceiveNote) { this.goodsReceiveNote = goodsReceiveNote; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public BigDecimal getOrderedQty() { return orderedQty; }
    public void setOrderedQty(BigDecimal orderedQty) { this.orderedQty = orderedQty; }
    public BigDecimal getReceivedQty() { return receivedQty; }
    public void setReceivedQty(BigDecimal receivedQty) { this.receivedQty = receivedQty; }
    public BigDecimal getRemainingQty() { return remainingQty; }
    public void setRemainingQty(BigDecimal remainingQty) { this.remainingQty = remainingQty; }
}
