package com.sme.erp.purchase.entity;

import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.purchase.enums.PurchaseReceiveStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goods_receive_notes")
public class GoodsReceiveNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grn_no", unique = true, nullable = false)
    private String grnNo;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "receive_date", nullable = false)
    private LocalDateTime receiveDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseReceiveStatus status = PurchaseReceiveStatus.POSTED;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @OneToMany(mappedBy = "goodsReceiveNote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiveItem> items = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        if (this.postedAt == null) {
            this.postedAt = now;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGrnNo() { return grnNo; }
    public void setGrnNo(String grnNo) { this.grnNo = grnNo; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public LocalDateTime getReceiveDate() { return receiveDate; }
    public void setReceiveDate(LocalDateTime receiveDate) { this.receiveDate = receiveDate; }
    public PurchaseReceiveStatus getStatus() { return status; }
    public void setStatus(PurchaseReceiveStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public List<GoodsReceiveItem> getItems() { return items; }
    public void setItems(List<GoodsReceiveItem> items) { this.items = items; }
}
