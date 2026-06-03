package com.sme.erp.customer.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sme.erp.enums.Status;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerDTO {

    private Long id;

    @Size(max = 50, message = "Customer code must be at most 50 characters")
    private String customerCode;

    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name must be at most 255 characters")
    private String name;

    @Size(max = 255, message = "Company name must be at most 255 characters")
    private String companyName;
    @JsonIgnore
    private boolean companyNamePresent;

    @Size(max = 255, message = "Contact person must be at most 255 characters")
    private String contactPerson;
    @JsonIgnore
    private boolean contactPersonPresent;

    @Size(max = 100, message = "Phone must be at most 100 characters")
    private String phone;
    @JsonIgnore
    private boolean phonePresent;

    @Email(message = "Email format is invalid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;
    @JsonIgnore
    private boolean emailPresent;

    private String address;
    @JsonIgnore
    private boolean addressPresent;

    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;
    @JsonIgnore
    private boolean cityPresent;

    @Size(max = 100, message = "Country must be at most 100 characters")
    private String country;
    @JsonIgnore
    private boolean countryPresent;

    @Size(max = 50, message = "Postal code must be at most 50 characters")
    private String postalCode;
    @JsonIgnore
    private boolean postalCodePresent;

    @DecimalMin(value = "0.00", inclusive = true, message = "Credit limit cannot be negative")
    private BigDecimal creditLimit;

    @DecimalMin(value = "0.00", inclusive = true, message = "Opening balance cannot be negative")
    private BigDecimal openingBalance;

    private BigDecimal currentBalance;

    @Size(max = 100, message = "Tax number must be at most 100 characters")
    private String taxNumber;
    @JsonIgnore
    private boolean taxNumberPresent;

    private Status status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        this.companyNamePresent = true;
    }
    public void normalizeCompanyName(String companyName) { this.companyName = companyName; }
    @JsonIgnore
    public boolean isCompanyNamePresent() { return companyNamePresent; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
        this.contactPersonPresent = true;
    }
    public void normalizeContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    @JsonIgnore
    public boolean isContactPersonPresent() { return contactPersonPresent; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) {
        this.phone = phone;
        this.phonePresent = true;
    }
    public void normalizePhone(String phone) { this.phone = phone; }
    @JsonIgnore
    public boolean isPhonePresent() { return phonePresent; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email;
        this.emailPresent = true;
    }
    public void normalizeEmail(String email) { this.email = email; }
    @JsonIgnore
    public boolean isEmailPresent() { return emailPresent; }

    public String getAddress() { return address; }
    public void setAddress(String address) {
        this.address = address;
        this.addressPresent = true;
    }
    public void normalizeAddress(String address) { this.address = address; }
    @JsonIgnore
    public boolean isAddressPresent() { return addressPresent; }

    public String getCity() { return city; }
    public void setCity(String city) {
        this.city = city;
        this.cityPresent = true;
    }
    public void normalizeCity(String city) { this.city = city; }
    @JsonIgnore
    public boolean isCityPresent() { return cityPresent; }

    public String getCountry() { return country; }
    public void setCountry(String country) {
        this.country = country;
        this.countryPresent = true;
    }
    public void normalizeCountry(String country) { this.country = country; }
    @JsonIgnore
    public boolean isCountryPresent() { return countryPresent; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        this.postalCodePresent = true;
    }
    public void normalizePostalCode(String postalCode) { this.postalCode = postalCode; }
    @JsonIgnore
    public boolean isPostalCodePresent() { return postalCodePresent; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
        this.taxNumberPresent = true;
    }
    public void normalizeTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
    @JsonIgnore
    public boolean isTaxNumberPresent() { return taxNumberPresent; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
