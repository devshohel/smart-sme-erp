package com.sme.erp.customer.receipt.entity;

import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.receipt.enums.CustomerReceiptAllocationMode;
import com.sme.erp.customer.receipt.enums.CustomerReceiptPaymentMethod;
import com.sme.erp.customer.receipt.enums.CustomerReceiptStatus;
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
@Table(name = "customer_receipts")
public class CustomerReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_no", nullable = false, unique = true)
    private String receiptNo;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private CustomerReceiptPaymentMethod paymentMethod;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "reference_no")
    private String referenceNo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_mode", nullable = false)
    private CustomerReceiptAllocationMode allocationMode = CustomerReceiptAllocationMode.AUTO;

    @Column(name = "total_allocated_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAllocatedAmount = BigDecimal.ZERO;

    @Column(name = "unapplied_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal unappliedAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "receipt", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<CustomerReceiptAllocation> allocations = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerReceiptStatus status = CustomerReceiptStatus.DRAFT;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.amount == null) {
            this.amount = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = CustomerReceiptStatus.DRAFT;
        }
        if (this.allocationMode == null) {
            this.allocationMode = CustomerReceiptAllocationMode.AUTO;
        }
        if (this.totalAllocatedAmount == null) {
            this.totalAllocatedAmount = BigDecimal.ZERO;
        }
        if (this.unappliedAmount == null) {
            this.unappliedAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.amount == null) {
            this.amount = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = CustomerReceiptStatus.DRAFT;
        }
        if (this.allocationMode == null) {
            this.allocationMode = CustomerReceiptAllocationMode.AUTO;
        }
        if (this.totalAllocatedAmount == null) {
            this.totalAllocatedAmount = BigDecimal.ZERO;
        }
        if (this.unappliedAmount == null) {
            this.unappliedAmount = BigDecimal.ZERO;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }

    public CustomerReceiptPaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(CustomerReceiptPaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public CustomerReceiptAllocationMode getAllocationMode() { return allocationMode; }
    public void setAllocationMode(CustomerReceiptAllocationMode allocationMode) { this.allocationMode = allocationMode; }

    public BigDecimal getTotalAllocatedAmount() { return totalAllocatedAmount; }
    public void setTotalAllocatedAmount(BigDecimal totalAllocatedAmount) { this.totalAllocatedAmount = totalAllocatedAmount; }

    public BigDecimal getUnappliedAmount() { return unappliedAmount; }
    public void setUnappliedAmount(BigDecimal unappliedAmount) { this.unappliedAmount = unappliedAmount; }

    public List<CustomerReceiptAllocation> getAllocations() { return allocations; }
    public void setAllocations(List<CustomerReceiptAllocation> allocations) { this.allocations = allocations; }

    public CustomerReceiptStatus getStatus() { return status; }
    public void setStatus(CustomerReceiptStatus status) { this.status = status; }

    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
