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
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
