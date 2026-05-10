package com.sme.erp.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WarehouseDTO {

    private Long id;

    @NotBlank(message = "Warehouse code is required")
    @Size(max = 50, message = "Warehouse code must be at most 50 characters")
    private String code;

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 255, message = "Warehouse name must be at most 255 characters")
    private String name;

    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;
    private Boolean active;

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
