package com.sme.erp.settings.dto;

import java.time.LocalDateTime;

public class InvoiceSettingsDTO {
    private Long id;
    private String salesInvoicePrefix;
    private String purchaseInvoicePrefix;
    private String salesOrderPrefix;
    private String purchaseOrderPrefix;
    private Integer nextInvoiceNumber;
    private String invoiceFooterText;
    private String defaultPaymentTerms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSalesInvoicePrefix() { return salesInvoicePrefix; }
    public void setSalesInvoicePrefix(String salesInvoicePrefix) { this.salesInvoicePrefix = salesInvoicePrefix; }
    public String getPurchaseInvoicePrefix() { return purchaseInvoicePrefix; }
    public void setPurchaseInvoicePrefix(String purchaseInvoicePrefix) { this.purchaseInvoicePrefix = purchaseInvoicePrefix; }
    public String getSalesOrderPrefix() { return salesOrderPrefix; }
    public void setSalesOrderPrefix(String salesOrderPrefix) { this.salesOrderPrefix = salesOrderPrefix; }
    public String getPurchaseOrderPrefix() { return purchaseOrderPrefix; }
    public void setPurchaseOrderPrefix(String purchaseOrderPrefix) { this.purchaseOrderPrefix = purchaseOrderPrefix; }
    public Integer getNextInvoiceNumber() { return nextInvoiceNumber; }
    public void setNextInvoiceNumber(Integer nextInvoiceNumber) { this.nextInvoiceNumber = nextInvoiceNumber; }
    public String getInvoiceFooterText() { return invoiceFooterText; }
    public void setInvoiceFooterText(String invoiceFooterText) { this.invoiceFooterText = invoiceFooterText; }
    public String getDefaultPaymentTerms() { return defaultPaymentTerms; }
    public void setDefaultPaymentTerms(String defaultPaymentTerms) { this.defaultPaymentTerms = defaultPaymentTerms; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
