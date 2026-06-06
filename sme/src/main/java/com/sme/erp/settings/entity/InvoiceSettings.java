package com.sme.erp.settings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_settings")
public class InvoiceSettings {
    @Id
    private Long id = 1L;

    @Column(name = "sales_invoice_prefix", nullable = false)
    private String salesInvoicePrefix;

    @Column(name = "purchase_invoice_prefix", nullable = false)
    private String purchaseInvoicePrefix;

    @Column(name = "sales_order_prefix", nullable = false)
    private String salesOrderPrefix;

    @Column(name = "purchase_order_prefix", nullable = false)
    private String purchaseOrderPrefix;

    @Column(name = "next_invoice_number", nullable = false)
    private Integer nextInvoiceNumber;

    @Column(name = "invoice_footer_text", columnDefinition = "TEXT")
    private String invoiceFooterText;

    @Column(name = "default_payment_terms")
    private String defaultPaymentTerms;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
