package com.sme.erp.supplier.dto;

import com.sme.erp.enums.Status;

public class SupplierOptionDTO {
    private Long id;
    private String supplierCode;
    private String name;
    private String phone;
    private Status status;

    public SupplierOptionDTO(Long id, String supplierCode, String name, String phone, Status status) {
        this.id = id;
        this.supplierCode = supplierCode;
        this.name = name;
        this.phone = phone;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getSupplierCode() { return supplierCode; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Status getStatus() { return status; }
}
