package com.sme.erp.sales.dto;

import jakarta.validation.constraints.NotBlank;

public class SalesReverseRequestDTO {

    @NotBlank(message = "Reversal reason is required")
    private String reversalReason;

    public String getReversalReason() {
        return reversalReason;
    }

    public void setReversalReason(String reversalReason) {
        this.reversalReason = reversalReason;
    }
}
