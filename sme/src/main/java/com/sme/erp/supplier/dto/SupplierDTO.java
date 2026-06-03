package com.sme.erp.supplier.dto;

import com.sme.erp.enums.Status;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SupplierDTO {

    private Long id;

    @Size(max = 50, message = "Supplier code must be at most 50 characters")
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    @Size(max = 255, message = "Supplier name must be at most 255 characters")
    private String name;

    @Size(max = 255, message = "Company name must be at most 255 characters")
    private String companyName;

    @Size(max = 255, message = "Contact person must be at most 255 characters")
    private String contactPerson;

    @Size(max = 100, message = "Phone must be at most 100 characters")
    private String phone;

    @Email(message = "Email format is invalid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    private String address;

    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @Size(max = 100, message = "Country must be at most 100 characters")
    private String country;

    @Size(max = 50, message = "Postal code must be at most 50 characters")
    private String postalCode;

    @DecimalMin(value = "0.00", inclusive = true, message = "Opening balance cannot be negative")
    private BigDecimal openingBalance;

    private BigDecimal currentBalance;

    @Size(max = 100, message = "Tax number must be at most 100 characters")
    private String taxNumber;

    @Size(max = 255, message = "Bank account must be at most 255 characters")
    private String bankAccount;

    @Size(max = 255, message = "Payment terms must be at most 255 characters")
    private String paymentTerms;

    private Status status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
