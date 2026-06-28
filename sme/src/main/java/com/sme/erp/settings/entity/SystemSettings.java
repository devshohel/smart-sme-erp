package com.sme.erp.settings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
public class SystemSettings {
    @Id
    private Long id = 1L;

    @Column(name = "default_currency", nullable = false)
    private String defaultCurrency;

    @Column(name = "date_format", nullable = false)
    private String dateFormat;

    @Column(name = "number_format", nullable = false)
    private String numberFormat;

    @Column(name = "low_stock_alert_enabled", nullable = false)
    private Boolean lowStockAlertEnabled = true;

    @Column(name = "dashboard_refresh_enabled", nullable = false)
    private Boolean dashboardRefreshEnabled = true;

    @Column(name = "enable_controlled_sales_mode", nullable = false)
    private Boolean enableControlledSalesMode = false;

    @Column(name = "enable_sales_orders", nullable = false)
    private Boolean enableSalesOrders = false;

    @Column(name = "enable_quotations", nullable = false)
    private Boolean enableQuotations = false;

    @Column(name = "enable_delivery_notes", nullable = false)
    private Boolean enableDeliveryNotes = false;

    @Column(name = "enable_sales_approval", nullable = false)
    private Boolean enableSalesApproval = false;

    @Column(name = "enable_manual_allocation", nullable = false)
    private Boolean enableManualAllocation = false;

    @Column(name = "enable_advanced_invoice", nullable = false)
    private Boolean enableAdvancedInvoice = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (lowStockAlertEnabled == null) {
            lowStockAlertEnabled = true;
        }
        if (dashboardRefreshEnabled == null) {
            dashboardRefreshEnabled = true;
        }
        applySalesFeatureDefaults();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
        applySalesFeatureDefaults();
    }

    private void applySalesFeatureDefaults() {
        if (enableControlledSalesMode == null) enableControlledSalesMode = false;
        if (enableSalesOrders == null) enableSalesOrders = false;
        if (enableQuotations == null) enableQuotations = false;
        if (enableDeliveryNotes == null) enableDeliveryNotes = false;
        if (enableSalesApproval == null) enableSalesApproval = false;
        if (enableManualAllocation == null) enableManualAllocation = false;
        if (enableAdvancedInvoice == null) enableAdvancedInvoice = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDefaultCurrency() { return defaultCurrency; }
    public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }
    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }
    public String getNumberFormat() { return numberFormat; }
    public void setNumberFormat(String numberFormat) { this.numberFormat = numberFormat; }
    public Boolean getLowStockAlertEnabled() { return lowStockAlertEnabled; }
    public void setLowStockAlertEnabled(Boolean lowStockAlertEnabled) { this.lowStockAlertEnabled = lowStockAlertEnabled; }
    public Boolean getDashboardRefreshEnabled() { return dashboardRefreshEnabled; }
    public void setDashboardRefreshEnabled(Boolean dashboardRefreshEnabled) { this.dashboardRefreshEnabled = dashboardRefreshEnabled; }
    public Boolean getEnableControlledSalesMode() { return enableControlledSalesMode; }
    public void setEnableControlledSalesMode(Boolean value) { this.enableControlledSalesMode = value; }
    public Boolean getEnableSalesOrders() { return enableSalesOrders; }
    public void setEnableSalesOrders(Boolean value) { this.enableSalesOrders = value; }
    public Boolean getEnableQuotations() { return enableQuotations; }
    public void setEnableQuotations(Boolean value) { this.enableQuotations = value; }
    public Boolean getEnableDeliveryNotes() { return enableDeliveryNotes; }
    public void setEnableDeliveryNotes(Boolean value) { this.enableDeliveryNotes = value; }
    public Boolean getEnableSalesApproval() { return enableSalesApproval; }
    public void setEnableSalesApproval(Boolean value) { this.enableSalesApproval = value; }
    public Boolean getEnableManualAllocation() { return enableManualAllocation; }
    public void setEnableManualAllocation(Boolean value) { this.enableManualAllocation = value; }
    public Boolean getEnableAdvancedInvoice() { return enableAdvancedInvoice; }
    public void setEnableAdvancedInvoice(Boolean value) { this.enableAdvancedInvoice = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
