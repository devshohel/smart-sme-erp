package com.sme.erp.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public class StockCardDTO {
    private Long productId;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal currentQuantity;
    private List<StockMovementDTO> movements;

    public StockCardDTO(Long productId, String productName, Long warehouseId, String warehouseName,
                        BigDecimal currentQuantity, List<StockMovementDTO> movements) {
        this.productId = productId;
        this.productName = productName;
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.currentQuantity = currentQuantity;
        this.movements = movements;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Long getWarehouseId() { return warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public List<StockMovementDTO> getMovements() { return movements; }
}
