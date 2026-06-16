package com.sme.erp.inventory.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class StockDTO {

	 private Long id;

	    @Positive(message = "Product id must be positive")
	    private Long productId;
	    private String productName;
	    private String sku;
	    private String barcode;
	    private Long categoryId;
	    private String categoryName;

	    @Positive(message = "Warehouse id must be positive")
	    private Long warehouseId;
	    private String warehouseName;

	    @PositiveOrZero(message = "Quantity must be zero or positive")
	    private BigDecimal quantity;

	    @PositiveOrZero(message = "Reorder level must be zero or positive")
	    private Integer reorderLevel;

    // getters setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getWarehouseId() { return warehouseId;}
	public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

	public String getWarehouseName() { return warehouseName; }
	public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
	
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
}
