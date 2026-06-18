package com.sme.erp.settings.entity;

import com.sme.erp.enums.Status;
import com.sme.erp.accounting.entity.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_settings")
public class TaxSettings {
    @Id
    private Long id = 1L;

    @Column(name = "tax_name", nullable = false)
    private String taxName;

    @Column(name = "tax_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "default_tax_enabled", nullable = false)
    private Boolean defaultTaxEnabled = false;

    @ManyToOne
    @JoinColumn(name = "tax_receivable_account_id")
    private Account taxReceivableAccount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = Status.ACTIVE;
        }
        if (defaultTaxEnabled == null) {
            defaultTaxEnabled = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
    public Account getTaxReceivableAccount() { return taxReceivableAccount; }
    public void setTaxReceivableAccount(Account taxReceivableAccount) { this.taxReceivableAccount = taxReceivableAccount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
