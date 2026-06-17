package com.sme.erp.customer.receipt.entity;

import com.sme.erp.sales.entity.SalesInvoice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_receipt_allocations")
public class CustomerReceiptAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receipt_id", nullable = false)
    private CustomerReceipt receipt;

    @ManyToOne
    @JoinColumn(name = "sales_invoice_id", nullable = false)
    private SalesInvoice salesInvoice;

    @Column(name = "allocated_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal allocatedAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.allocatedAmount == null) {
            this.allocatedAmount = BigDecimal.ZERO;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CustomerReceipt getReceipt() { return receipt; }
    public void setReceipt(CustomerReceipt receipt) { this.receipt = receipt; }
    public SalesInvoice getSalesInvoice() { return salesInvoice; }
    public void setSalesInvoice(SalesInvoice salesInvoice) { this.salesInvoice = salesInvoice; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
