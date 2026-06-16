package com.sme.erp.product.dto;

import com.sme.erp.enums.Status;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ProductBulkStatusUpdateDTO {
    @NotEmpty(message = "Product ids are required")
    private List<Long> productIds;

    @NotNull(message = "Status is required")
    private Status status;

    public List<Long> getProductIds() { return productIds; }
    public void setProductIds(List<Long> productIds) { this.productIds = productIds; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
