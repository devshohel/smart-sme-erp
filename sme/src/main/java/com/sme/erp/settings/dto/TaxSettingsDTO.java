package com.sme.erp.settings.dto;

import com.sme.erp.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TaxSettingsDTO {
    private Long id;
    private String taxName;
    private BigDecimal taxRate;
    private Status status;
    private Boolean defaultTaxEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTaxName() { return taxName; }
    public void setTaxName(String taxName) { this.taxName = taxName; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Boolean getDefaultTaxEnabled() { return defaultTaxEnabled; }
    public void setDefaultTaxEnabled(Boolean defaultTaxEnabled) { this.defaultTaxEnabled = defaultTaxEnabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
