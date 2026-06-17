package com.sme.erp.customer.receipt.dto;

import com.sme.erp.customer.receipt.enums.CustomerReceiptPaymentMethod;
import com.sme.erp.customer.receipt.enums.CustomerReceiptStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerReceiptDTO {
    private Long id;
    private String receiptNo;

    @NotNull(message = "Customer is required")
    private Long customerId;
    private String customerCode;
    private String customerName;

    @NotNull(message = "Receipt date is required")
    private LocalDate receiptDate;

    @NotNull(message = "Payment method is required")
    private CustomerReceiptPaymentMethod paymentMethod;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String referenceNo;
    private String notes;
    private CustomerReceiptStatus status;
    private LocalDateTime postedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String journalNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
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
    public String getJournalNo() { return journalNo; }
    public void setJournalNo(String journalNo) { this.journalNo = journalNo; }
}
