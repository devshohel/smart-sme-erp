package com.sme.erp.customer.dto;

import com.sme.erp.customer.receipt.dto.CustomerReceiptDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CustomerDetailDTO {
    private CustomerDTO customer;
    private BigDecimal availableCredit;
    private String balanceStatus;
    private long totalSalesInvoices;
    private BigDecimal totalDue;
    private LocalDateTime lastInvoiceDate;
    private LocalDateTime lastPaymentDate;
    private List<CustomerReceiptDTO> recentReceipts;
    private List<CustomerTransactionDTO> recentSalesInvoices;
    private List<CustomerTransactionDTO> recentSalesReturns;

    public CustomerDetailDTO(CustomerDTO customer, BigDecimal availableCredit, String balanceStatus,
                             long totalSalesInvoices, BigDecimal totalDue, LocalDateTime lastInvoiceDate,
                             LocalDateTime lastPaymentDate, List<CustomerReceiptDTO> recentReceipts,
                             List<CustomerTransactionDTO> recentSalesInvoices,
                             List<CustomerTransactionDTO> recentSalesReturns) {
        this.customer = customer;
        this.availableCredit = availableCredit;
        this.balanceStatus = balanceStatus;
        this.totalSalesInvoices = totalSalesInvoices;
        this.totalDue = totalDue;
        this.lastInvoiceDate = lastInvoiceDate;
        this.lastPaymentDate = lastPaymentDate;
        this.recentReceipts = recentReceipts;
        this.recentSalesInvoices = recentSalesInvoices;
        this.recentSalesReturns = recentSalesReturns;
    }

    public CustomerDTO getCustomer() { return customer; }
    public BigDecimal getAvailableCredit() { return availableCredit; }
    public String getBalanceStatus() { return balanceStatus; }
    public long getTotalSalesInvoices() { return totalSalesInvoices; }
    public BigDecimal getTotalDue() { return totalDue; }
    public LocalDateTime getLastInvoiceDate() { return lastInvoiceDate; }
    public LocalDateTime getLastPaymentDate() { return lastPaymentDate; }
    public List<CustomerReceiptDTO> getRecentReceipts() { return recentReceipts; }
    public List<CustomerTransactionDTO> getRecentSalesInvoices() { return recentSalesInvoices; }
    public List<CustomerTransactionDTO> getRecentSalesReturns() { return recentSalesReturns; }
}
