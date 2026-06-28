package com.sme.erp.sales.dto;

import com.sme.erp.sales.enums.SalesInvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesReturnContextDTO {
    private Long invoiceId;
    private String invoiceNo;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime saleDate;
    private SalesInvoiceStatus status;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
    private boolean paidRefundSupported;
    private String limitationMessage;
    private List<SalesReturnContextItemDTO> items = new ArrayList<>();

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    public SalesInvoiceStatus getStatus() { return status; }
    public void setStatus(SalesInvoiceStatus status) { this.status = status; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getDueAmount() { return dueAmount; }
    public void setDueAmount(BigDecimal dueAmount) { this.dueAmount = dueAmount; }
    public boolean isPaidRefundSupported() { return paidRefundSupported; }
    public void setPaidRefundSupported(boolean paidRefundSupported) { this.paidRefundSupported = paidRefundSupported; }
    public String getLimitationMessage() { return limitationMessage; }
    public void setLimitationMessage(String limitationMessage) { this.limitationMessage = limitationMessage; }
    public List<SalesReturnContextItemDTO> getItems() { return items; }
    public void setItems(List<SalesReturnContextItemDTO> items) { this.items = items; }
}
