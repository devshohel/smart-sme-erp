package com.sme.erp.settings.dto;

import java.time.LocalDateTime;

public class SystemSettingsDTO {
    private Long id;
    private String defaultCurrency;
    private String dateFormat;
    private String numberFormat;
    private Boolean lowStockAlertEnabled;
    private Boolean dashboardRefreshEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
