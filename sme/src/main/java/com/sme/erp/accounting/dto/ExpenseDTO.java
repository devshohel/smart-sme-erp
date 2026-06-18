package com.sme.erp.accounting.dto;

import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseDTO {
    private Long id;
    private String expenseNo;
    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;
    @NotNull(message = "Category is required")
    private Long categoryId;
    private String categoryName;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
    @NotNull(message = "Payment method is required")
    private AccountingPaymentMethod paymentMethod;
    private String referenceNo;
    private String notes;
    private ExpenseStatus status;
    private String createdBy;
    private LocalDateTime submittedAt;
    private String submittedBy;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private LocalDateTime rejectedAt;
    private String rejectedBy;
    private String rejectionReason;
    private String approvalComment;
    private String receiptOriginalFilename;
    private String receiptStoredFilename;
    private String receiptContentType;
    private Long receiptSize;
    private String receiptUrl;
    private Long journalEntryId;
    private Boolean taxApplicable;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal grossAmount;
    private LocalDateTime reversedAt;
    private String reversedBy;
    private String reversalReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExpenseNo() { return expenseNo; }
    public void setExpenseNo(String expenseNo) { this.expenseNo = expenseNo; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public AccountingPaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(AccountingPaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public ExpenseStatus getStatus() { return status; }
    public void setStatus(ExpenseStatus status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getApprovalComment() { return approvalComment; }
    public void setApprovalComment(String approvalComment) { this.approvalComment = approvalComment; }
    public String getReceiptOriginalFilename() { return receiptOriginalFilename; }
    public void setReceiptOriginalFilename(String receiptOriginalFilename) { this.receiptOriginalFilename = receiptOriginalFilename; }
    public String getReceiptStoredFilename() { return receiptStoredFilename; }
    public void setReceiptStoredFilename(String receiptStoredFilename) { this.receiptStoredFilename = receiptStoredFilename; }
    public String getReceiptContentType() { return receiptContentType; }
    public void setReceiptContentType(String receiptContentType) { this.receiptContentType = receiptContentType; }
    public Long getReceiptSize() { return receiptSize; }
    public void setReceiptSize(Long receiptSize) { this.receiptSize = receiptSize; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public Long getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(Long journalEntryId) { this.journalEntryId = journalEntryId; }
    public Boolean getTaxApplicable() { return taxApplicable; }
    public void setTaxApplicable(Boolean taxApplicable) { this.taxApplicable = taxApplicable; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public LocalDateTime getReversedAt() { return reversedAt; }
    public void setReversedAt(LocalDateTime reversedAt) { this.reversedAt = reversedAt; }
    public String getReversedBy() { return reversedBy; }
    public void setReversedBy(String reversedBy) { this.reversedBy = reversedBy; }
    public String getReversalReason() { return reversalReason; }
    public void setReversalReason(String reversalReason) { this.reversalReason = reversalReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
