package com.sme.erp.sales.entity;

import com.sme.erp.product.entity.Product;
import com.sme.erp.sales.enums.SalesReturnCondition;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_return_items")
public class SalesReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "return_id", nullable = false)
    private SalesReturn returnEntity;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Product product;

    @Column(name = "invoice_item_id")
    private Long invoiceItemId;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition", nullable = false, length = 40)
    private SalesReturnCondition condition = SalesReturnCondition.RESELLABLE;

    @Column(nullable = false)
    private Boolean restock = true;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SalesReturn getReturnEntity() { return returnEntity; }
    public void setReturnEntity(SalesReturn returnEntity) { this.returnEntity = returnEntity; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
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
