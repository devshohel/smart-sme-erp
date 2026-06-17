package com.sme.erp.customer.receipt.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerReceiptAllocationDTO {
    private Long id;

    @NotNull(message = "Sales invoice is required")
    private Long salesInvoiceId;
    private String invoiceNo;
    private LocalDateTime invoiceDate;
    private BigDecimal netTotal;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;

    @NotNull(message = "Allocated amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Allocated amount must be greater than zero")
    private BigDecimal allocatedAmount;

    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSalesInvoiceId() { return salesInvoiceId; }
    public void setSalesInvoiceId(Long salesInvoiceId) { this.salesInvoiceId = salesInvoiceId; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public LocalDateTime getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDateTime invoiceDate) { this.invoiceDate = invoiceDate; }
    public BigDecimal getNetTotal() { return netTotal; }
    public void setNetTotal(BigDecimal netTotal) { this.netTotal = netTotal; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getDueAmount() { return dueAmount; }
    public void setDueAmount(BigDecimal dueAmount) { this.dueAmount = dueAmount; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
