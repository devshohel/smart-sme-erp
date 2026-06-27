package com.sme.erp.sales.pos.dto;

import com.sme.erp.sales.pos.enums.PosPaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PosPaymentRequestDTO {
    @NotNull(message = "Payment method is required")
    private PosPaymentMethod paymentMethod;

    @NotNull(message = "Paid amount is required")
    @DecimalMin(value = "0.00", message = "Paid amount cannot be negative")
    private BigDecimal paidAmount;

    @Size(max = 255, message = "Payment reference must be at most 255 characters")
    private String referenceNo;

    public PosPaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PosPaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
}
