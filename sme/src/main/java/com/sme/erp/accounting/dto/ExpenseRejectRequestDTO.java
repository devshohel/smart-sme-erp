package com.sme.erp.accounting.dto;

import jakarta.validation.constraints.NotBlank;

public class ExpenseRejectRequestDTO {
    @NotBlank(message = "Rejection reason is required")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
