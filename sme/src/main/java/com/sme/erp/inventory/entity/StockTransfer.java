package com.sme.erp.inventory.entity;

import com.sme.erp.inventory.enums.StockTransferStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_transfers", indexes = {
        @Index(name = "idx_stock_transfer_no", columnList = "transfer_no"),
        @Index(name = "idx_stock_transfer_status", columnList = "status"),
        @Index(name = "idx_stock_transfer_date", columnList = "transfer_date")
})
public class StockTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_no", unique = true, nullable = false)
    private String transferNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_warehouse_id", nullable = false)
    private Warehouse fromWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_warehouse_id", nullable = false)
    private Warehouse toWarehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockTransferStatus status = StockTransferStatus.DRAFT;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    @Column(length = 1000)
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String receivedBy;
    private LocalDateTime receivedAt;
    private String reversedBy;
    private LocalDateTime reversedAt;
    @Column(length = 500)
    private String reversalReason;

    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTransferItem> items = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (transferDate == null) {
            transferDate = LocalDate.now();
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransferNo() { return transferNo; }
    public void setTransferNo(String transferNo) { this.transferNo = transferNo; }
    public Warehouse getFromWarehouse() { return fromWarehouse; }
    public void setFromWarehouse(Warehouse fromWarehouse) { this.fromWarehouse = fromWarehouse; }
    public Warehouse getToWarehouse() { return toWarehouse; }
    public void setToWarehouse(Warehouse toWarehouse) { this.toWarehouse = toWarehouse; }
    public StockTransferStatus getStatus() { return status; }
    public void setStatus(StockTransferStatus status) { this.status = status; }
    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public String getReversedBy() { return reversedBy; }
    public void setReversedBy(String reversedBy) { this.reversedBy = reversedBy; }
    public LocalDateTime getReversedAt() { return reversedAt; }
    public void setReversedAt(LocalDateTime reversedAt) { this.reversedAt = reversedAt; }
    public String getReversalReason() { return reversalReason; }
    public void setReversalReason(String reversalReason) { this.reversalReason = reversalReason; }
    public List<StockTransferItem> getItems() { return items; }
    public void setItems(List<StockTransferItem> items) { this.items = items; }
}
