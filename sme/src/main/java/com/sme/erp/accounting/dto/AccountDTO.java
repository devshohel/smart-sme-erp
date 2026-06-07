package com.sme.erp.accounting.dto;

import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AccountDTO {
    private Long id;
    @NotBlank(message = "Account code is required")
    private String accountCode;
    @NotBlank(message = "Account name is required")
    private String accountName;
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    private Long parentAccountId;
    private String parentAccountName;
    private Status status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public Long getParentAccountId() { return parentAccountId; }
    public void setParentAccountId(Long parentAccountId) { this.parentAccountId = parentAccountId; }
    public String getParentAccountName() { return parentAccountName; }
    public void setParentAccountName(String parentAccountName) { this.parentAccountName = parentAccountName; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
