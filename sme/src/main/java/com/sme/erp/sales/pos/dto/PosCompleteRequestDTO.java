package com.sme.erp.sales.pos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PosCompleteRequestDTO {
    @NotNull(message = "Customer id is required")
    @Positive(message = "Customer id must be positive")
    private Long customerId;

    @NotNull(message = "Warehouse id is required")
    @Positive(message = "Warehouse id must be positive")
    private Long warehouseId;

    @NotNull(message = "Sale date is required")
    private LocalDateTime saleDate;

    @Valid
    @NotEmpty(message = "At least one sale item is required")
    private List<PosItemRequestDTO> items = new ArrayList<>();

    @Valid
    @NotNull(message = "Payment is required")
    private PosPaymentRequestDTO payment;

    @Size(max = 500, message = "Notes must be at most 500 characters")
    private String notes;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    public List<PosItemRequestDTO> getItems() { return items; }
    public void setItems(List<PosItemRequestDTO> items) { this.items = items; }
    public PosPaymentRequestDTO getPayment() { return payment; }
    public void setPayment(PosPaymentRequestDTO payment) { this.payment = payment; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
