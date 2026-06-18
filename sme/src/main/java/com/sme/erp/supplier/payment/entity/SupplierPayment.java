package com.sme.erp.supplier.payment.entity;

import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.payment.enums.SupplierPaymentAllocationMode;
import com.sme.erp.supplier.payment.enums.SupplierPaymentMethod;
import com.sme.erp.supplier.payment.enums.SupplierPaymentStatus;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supplier_payments")
public class SupplierPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_no", nullable = false, unique = true)
    private String paymentNo;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private SupplierPaymentMethod paymentMethod;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "reference_no")
    private String referenceNo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_mode", nullable = false)
    private SupplierPaymentAllocationMode allocationMode = SupplierPaymentAllocationMode.AUTO;

    @Column(name = "total_allocated_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAllocatedAmount = BigDecimal.ZERO;

    @Column(name = "unapplied_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal unappliedAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "supplierPayment", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<SupplierPaymentAllocation> allocations = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierPaymentStatus status = SupplierPaymentStatus.DRAFT;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "reversed_at")
    private LocalDateTime reversedAt;

    @Column(name = "reversal_reason", columnDefinition = "TEXT")
    private String reversalReason;

    @Column(name = "reversed_by")
    private Long reversedBy;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        applyDefaults();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        applyDefaults();
    }

    private void applyDefaults() {
        if (this.amount == null) this.amount = BigDecimal.ZERO;
        if (this.status == null) this.status = SupplierPaymentStatus.DRAFT;
        if (this.allocationMode == null) this.allocationMode = SupplierPaymentAllocationMode.AUTO;
        if (this.totalAllocatedAmount == null) this.totalAllocatedAmount = BigDecimal.ZERO;
        if (this.unappliedAmount == null) this.unappliedAmount = BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public SupplierPaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(SupplierPaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public SupplierPaymentAllocationMode getAllocationMode() { return allocationMode; }
    public void setAllocationMode(SupplierPaymentAllocationMode allocationMode) { this.allocationMode = allocationMode; }
    public BigDecimal getTotalAllocatedAmount() { return totalAllocatedAmount; }
    public void setTotalAllocatedAmount(BigDecimal totalAllocatedAmount) { this.totalAllocatedAmount = totalAllocatedAmount; }
    public BigDecimal getUnappliedAmount() { return unappliedAmount; }
    public void setUnappliedAmount(BigDecimal unappliedAmount) { this.unappliedAmount = unappliedAmount; }
    public List<SupplierPaymentAllocation> getAllocations() { return allocations; }
    public void setAllocations(List<SupplierPaymentAllocation> allocations) { this.allocations = allocations; }
    public SupplierPaymentStatus getStatus() { return status; }
    public void setStatus(SupplierPaymentStatus status) { this.status = status; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public LocalDateTime getReversedAt() { return reversedAt; }
    public void setReversedAt(LocalDateTime reversedAt) { this.reversedAt = reversedAt; }
    public String getReversalReason() { return reversalReason; }
    public void setReversalReason(String reversalReason) { this.reversalReason = reversalReason; }
    public Long getReversedBy() { return reversedBy; }
    public void setReversedBy(Long reversedBy) { this.reversedBy = reversedBy; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
