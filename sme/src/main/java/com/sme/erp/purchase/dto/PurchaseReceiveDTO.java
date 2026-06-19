package com.sme.erp.purchase.dto;

import com.sme.erp.purchase.enums.PurchaseReceiveStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseReceiveDTO {
    private Long id;
    private String grnNo;
    private Long purchaseOrderId;
    private String purchaseCode;
    private Long warehouseId;
    private String warehouseName;

    @NotNull(message = "Receive date is required")
    private LocalDateTime receiveDate;

    private PurchaseReceiveStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime postedAt;

    @Valid
    @NotNull(message = "Receive items are required")
    private List<PurchaseReceiveItemDTO> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGrnNo() { return grnNo; }
    public void setGrnNo(String grnNo) { this.grnNo = grnNo; }
    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public String getPurchaseCode() { return purchaseCode; }
    public void setPurchaseCode(String purchaseCode) { this.purchaseCode = purchaseCode; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public LocalDateTime getReceiveDate() { return receiveDate; }
    public void setReceiveDate(LocalDateTime receiveDate) { this.receiveDate = receiveDate; }
    public PurchaseReceiveStatus getStatus() { return status; }
    public void setStatus(PurchaseReceiveStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public List<PurchaseReceiveItemDTO> getItems() { return items; }
    public void setItems(List<PurchaseReceiveItemDTO> items) { this.items = items; }
}
