package com.sme.erp.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseReturnDTO {

    private Long id;
    private String returnCode;

    @NotNull(message = "Purchase id is required")
    @Positive(message = "Purchase id must be positive")
    private Long purchaseId;

    private String purchaseCode;

    @NotNull(message = "Supplier id is required")
    @Positive(message = "Supplier id must be positive")
    private Long supplierId;

    private String supplierName;

    @NotNull(message = "Return date is required")
    private LocalDateTime returnDate;

    private BigDecimal totalAmount;
    private Long createdBy;
    private LocalDateTime createdAt;

    @Valid
    @NotNull(message = "Return items are required")
    private List<PurchaseReturnItemDTO> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReturnCode() { return returnCode; }
    public void setReturnCode(String returnCode) { this.returnCode = returnCode; }

    public Long getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Long purchaseId) { this.purchaseId = purchaseId; }

    public String getPurchaseCode() { return purchaseCode; }
    public void setPurchaseCode(String purchaseCode) { this.purchaseCode = purchaseCode; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<PurchaseReturnItemDTO> getItems() { return items; }
    public void setItems(List<PurchaseReturnItemDTO> items) { this.items = items; }
}
