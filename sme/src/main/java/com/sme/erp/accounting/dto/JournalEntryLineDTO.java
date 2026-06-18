package com.sme.erp.accounting.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class JournalEntryLineDTO {
    private Long id;
    @NotNull(message = "Account is required")
    private Long accountId;
    private String accountCode;
    private String accountName;
    private Long costCenterId;
    private String costCenterCode;
    private String costCenterName;
    private BigDecimal debit = BigDecimal.ZERO;
    private BigDecimal credit = BigDecimal.ZERO;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public Long getCostCenterId() { return costCenterId; }
    public void setCostCenterId(Long v) { costCenterId = v; }
    public String getCostCenterCode() { return costCenterCode; }
    public void setCostCenterCode(String v) { costCenterCode = v; }
    public String getCostCenterName() { return costCenterName; }
    public void setCostCenterName(String v) { costCenterName = v; }
    public BigDecimal getDebit() { return debit; }
    public void setDebit(BigDecimal debit) { this.debit = debit; }
    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
