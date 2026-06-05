package com.sme.erp.sales.entity;

import com.sme.erp.customer.entity.Customer;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.enums.SalesPaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_invoices")
public class SalesInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_no", unique = true, nullable = false)
    private String invoiceNo;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private SalesOrder order;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "net_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal netTotal = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "due_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal dueAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private SalesPaymentStatus paymentStatus = SalesPaymentStatus.DUE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesInvoiceStatus status = SalesInvoiceStatus.PENDING;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesItem> items = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }

    public SalesOrder getOrder() { return order; }
    public void setOrder(SalesOrder order) { this.order = order; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getNetTotal() { return netTotal; }
    public void setNetTotal(BigDecimal netTotal) { this.netTotal = netTotal; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public BigDecimal getDueAmount() { return dueAmount; }
    public void setDueAmount(BigDecimal dueAmount) { this.dueAmount = dueAmount; }

    public SalesPaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(SalesPaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public SalesInvoiceStatus getStatus() { return status; }
    public void setStatus(SalesInvoiceStatus status) { this.status = status; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<SalesItem> getItems() { return items; }
    public void setItems(List<SalesItem> items) { this.items = items; }
}
