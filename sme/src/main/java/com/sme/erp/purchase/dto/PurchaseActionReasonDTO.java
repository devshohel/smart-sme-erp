package com.sme.erp.purchase.dto;

import jakarta.validation.constraints.NotBlank;

public class PurchaseActionReasonDTO {
    @NotBlank(message = "Reason is required")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
