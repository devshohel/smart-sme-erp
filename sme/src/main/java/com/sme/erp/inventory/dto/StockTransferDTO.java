package com.sme.erp.inventory.dto;

import com.sme.erp.inventory.enums.StockTransferStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockTransferDTO {
    private Long id;
    private String transferNo;

    @NotNull
    @Positive
    private Long fromWarehouseId;
    private String fromWarehouseName;

    @NotNull
    @Positive
    private Long toWarehouseId;
    private String toWarehouseName;

    private StockTransferStatus status = StockTransferStatus.DRAFT;

    @NotNull
    private LocalDate transferDate;
    private LocalDate expectedDate;
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
    private String reversalReason;

    @Valid
    @NotEmpty
    private List<StockTransferItemDTO> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransferNo() { return transferNo; }
    public void setTransferNo(String transferNo) { this.transferNo = transferNo; }
    public Long getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(Long fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }
    public String getFromWarehouseName() { return fromWarehouseName; }
    public void setFromWarehouseName(String fromWarehouseName) { this.fromWarehouseName = fromWarehouseName; }
    public Long getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(Long toWarehouseId) { this.toWarehouseId = toWarehouseId; }
    public String getToWarehouseName() { return toWarehouseName; }
    public void setToWarehouseName(String toWarehouseName) { this.toWarehouseName = toWarehouseName; }
    public StockTransferStatus getStatus() { return status; }
    public void setStatus(StockTransferStatus status) { this.status = status; }
    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
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
    public List<StockTransferItemDTO> getItems() { return items; }
    public void setItems(List<StockTransferItemDTO> items) { this.items = items; }
}
