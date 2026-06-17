package com.sme.erp.customer.dto;

import java.math.BigDecimal;

public class CustomerAgingRowDTO {
    private Long customerId;
    private String customerCode;
    private String customerName;
    private BigDecimal current;
    private BigDecimal days1To30;
    private BigDecimal days31To60;
    private BigDecimal days61To90;
    private BigDecimal days90Plus;
    private BigDecimal totalDue;

    public CustomerAgingRowDTO(Long customerId, String customerCode, String customerName,
                               BigDecimal current, BigDecimal days1To30, BigDecimal days31To60,
                               BigDecimal days61To90, BigDecimal days90Plus, BigDecimal totalDue) {
        this.customerId = customerId;
        this.customerCode = customerCode;
        this.customerName = customerName;
        this.current = current;
        this.days1To30 = days1To30;
        this.days31To60 = days31To60;
        this.days61To90 = days61To90;
        this.days90Plus = days90Plus;
        this.totalDue = totalDue;
    }

    public Long getCustomerId() { return customerId; }
    public String getCustomerCode() { return customerCode; }
    public String getCustomerName() { return customerName; }
    public BigDecimal getCurrent() { return current; }
    public BigDecimal getDays1To30() { return days1To30; }
    public BigDecimal getDays31To60() { return days31To60; }
    public BigDecimal getDays61To90() { return days61To90; }
    public BigDecimal getDays90Plus() { return days90Plus; }
    public BigDecimal getTotalDue() { return totalDue; }
}
