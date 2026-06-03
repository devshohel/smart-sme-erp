package com.sme.erp.sales.dto;

import com.sme.erp.sales.enums.SalesOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderDTO {

    private Long id;
    private String orderNo;

    @NotNull(message = "Customer id is required")
    @Positive(message = "Customer id must be positive")
    private Long customerId;

    private String customerName;

    @NotNull(message = "Warehouse id is required")
    @Positive(message = "Warehouse id must be positive")
    private Long warehouseId;

    private String warehouseName;

    @NotNull(message = "Order date is required")
    private LocalDateTime orderDate;

    private SalesOrderStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private String notes;

    @Valid
    private List<SalesItemDTO> items = new ArrayList<>();

    private BigDecimal grandTotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public SalesOrderStatus getStatus() { return status; }
    public void setStatus(SalesOrderStatus status) { this.status = status; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<SalesItemDTO> getItems() { return items; }
    public void setItems(List<SalesItemDTO> items) { this.items = items; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
}
