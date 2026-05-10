package com.sme.erp.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ProductDTO {

    private Long id;

    @Size(max = 50, message = "Product code must be at most 50 characters")
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must be at most 255 characters")
    private String productName;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must be at most 100 characters")
    private String sku;

    @NotNull(message = "Purchase price required")
    private BigDecimal purchasePrice;

    @NotNull(message = "Sale price required")
    private BigDecimal salePrice;

    @Positive(message = "Category id must be positive")
    private Long categoryId;
    private String categoryName;

    @Positive(message = "Brand id must be positive")
    private Long brandId;
    private String brandName;

    @Positive(message = "UOM id must be positive")
    private Long uomId;
    private String uomName;

    // Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public Long getUomId() { return uomId; }
    public void setUomId(Long uomId) { this.uomId = uomId; }

    public String getUomName() { return uomName; }
    public void setUomName(String uomName) { this.uomName = uomName; }
}
