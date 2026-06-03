package com.sme.erp.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesReturnDTO {

    private Long id;
    private String returnCode;

    @NotNull(message = "Invoice id is required")
    @Positive(message = "Invoice id must be positive")
    private Long invoiceId;

    private String invoiceNo;

    @NotNull(message = "Customer id is required")
    @Positive(message = "Customer id must be positive")
    private Long customerId;

    private String customerName;

    @NotNull(message = "Return date is required")
    private LocalDateTime returnDate;

    private BigDecimal totalAmount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private String notes;

    @Valid
    @NotNull(message = "Return items are required")
    private List<SalesReturnItemDTO> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReturnCode() { return returnCode; }
    public void setReturnCode(String returnCode) { this.returnCode = returnCode; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<SalesReturnItemDTO> getItems() { return items; }
    public void setItems(List<SalesReturnItemDTO> items) { this.items = items; }
}
