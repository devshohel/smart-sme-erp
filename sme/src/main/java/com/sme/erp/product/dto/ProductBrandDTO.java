package com.sme.erp.product.dto;

import com.sme.erp.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductBrandDTO {

    private Long id;

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must be at most 50 characters")
    private String code;

    @NotBlank(message = "Brand name is required")
    @Size(max = 255, message = "Brand name must be at most 255 characters")
    private String brandName;

    private Status status;

    // getters setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
