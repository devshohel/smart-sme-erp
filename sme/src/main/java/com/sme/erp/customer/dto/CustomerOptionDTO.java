package com.sme.erp.customer.dto;

import java.math.BigDecimal;

public class CustomerOptionDTO {
    private Long id;
    private String customerCode;
    private String name;
    private String phone;
    private BigDecimal currentBalance;

    public CustomerOptionDTO(Long id, String customerCode, String name, String phone, BigDecimal currentBalance) {
        this.id = id;
        this.customerCode = customerCode;
        this.name = name;
        this.phone = phone;
        this.currentBalance = currentBalance;
    }

    public Long getId() { return id; }
    public String getCustomerCode() { return customerCode; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
}
