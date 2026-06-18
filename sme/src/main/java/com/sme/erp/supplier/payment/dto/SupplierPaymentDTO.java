package com.sme.erp.supplier.payment.dto;

import com.sme.erp.supplier.payment.enums.SupplierPaymentAllocationMode;
import com.sme.erp.supplier.payment.enums.SupplierPaymentMethod;
import com.sme.erp.supplier.payment.enums.SupplierPaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SupplierPaymentDTO {
    private Long id;
    private String paymentNo;

    @NotNull(message = "Supplier is required")
    private Long supplierId;
    private String supplierCode;
    private String supplierName;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Payment method is required")
    private SupplierPaymentMethod paymentMethod;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String referenceNo;
    private String notes;
    private SupplierPaymentAllocationMode allocationMode;
    private BigDecimal totalAllocatedAmount;
    private BigDecimal unappliedAmount;
    private List<SupplierPaymentAllocationDTO> allocations = new ArrayList<>();
    private SupplierPaymentStatus status;
    private LocalDateTime postedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime reversedAt;
    private String reversalReason;
    private Long reversedBy;
    private boolean canReverse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String journalNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
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
    public List<SupplierPaymentAllocationDTO> getAllocations() { return allocations; }
    public void setAllocations(List<SupplierPaymentAllocationDTO> allocations) { this.allocations = allocations; }
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
    public boolean isCanReverse() { return canReverse; }
    public void setCanReverse(boolean canReverse) { this.canReverse = canReverse; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getJournalNo() { return journalNo; }
    public void setJournalNo(String journalNo) { this.journalNo = journalNo; }
}
