package com.sme.erp.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ProductCategoryDTO {

    private Long id;

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must be at most 50 characters")
    private String code;

    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name must be at most 255 characters")
    private String categoryName;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @Positive(message = "Parent category id must be positive")
    private Long parentCategoryId;

    // ===== Getter Setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(Long parentCategoryId) { this.parentCategoryId = parentCategoryId; }
}
