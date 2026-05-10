package com.sme.erp.inventory.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class StockDTO {

	 private Long id;

	    @Positive(message = "Product id must be positive")
	    private Long productId;
	    private String productName;

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

    public Long getWarehouseId() { return warehouseId;}
	public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

	public String getWarehouseName() { return warehouseName; }
	public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
	
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
}
