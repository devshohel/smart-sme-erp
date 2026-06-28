package com.sme.erp.sales.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.sme.erp.sales.enums.SalesReturnCondition;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class SalesReturnItemDTO {

    private Long id;

    @NotNull(message = "Product id is required")
    @Positive(message = "Product id must be positive")
    private Long productId;

    private String productName;
    private Long invoiceItemId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    private BigDecimal discount;
    private BigDecimal tax;

    @Size(max = 500, message = "Return reason must be at most 500 characters")
    private String returnReason;
    private SalesReturnCondition condition;
    private Boolean restock;

    private BigDecimal total;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getInvoiceItemId() { return invoiceItemId; }
    public void setInvoiceItemId(Long invoiceItemId) { this.invoiceItemId = invoiceItemId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    public SalesReturnCondition getCondition() { return condition; }
    public void setCondition(SalesReturnCondition condition) { this.condition = condition; }
    public Boolean getRestock() { return restock; }
    public void setRestock(Boolean restock) { this.restock = restock; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
