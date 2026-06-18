package com.sme.erp.accounting.entity;

import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounting_expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String expenseNo;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountingPaymentMethod paymentMethod;

    private String referenceNo;
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    private String createdBy;
    private LocalDateTime submittedAt;
    private String submittedBy;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private LocalDateTime rejectedAt;
    private String rejectedBy;
    @Column(length = 1000)
    private String rejectionReason;
    @Column(length = 1000)
    private String approvalComment;
    private String receiptOriginalFilename;
    private String receiptStoredFilename;
    private String receiptContentType;
    private Long receiptSize;
    private String receiptPath;
    private String receiptUrl;
    private Boolean taxApplicable = false;
    @Column(precision = 10, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;
    @Column(precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    @Column(precision = 19, scale = 2)
    private BigDecimal netAmount = BigDecimal.ZERO;
    @Column(precision = 19, scale = 2)
    private BigDecimal grossAmount = BigDecimal.ZERO;
    private LocalDateTime reversedAt;
    private String reversedBy;
    @Column(length = 1000)
    private String reversalReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExpenseNo() { return expenseNo; }
    public void setExpenseNo(String expenseNo) { this.expenseNo = expenseNo; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }
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
    public String getReceiptPath() { return receiptPath; }
    public void setReceiptPath(String receiptPath) { this.receiptPath = receiptPath; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
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
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
