package com.sme.erp.auth.dto;

public class RoleDTO {
    private Long id;
    private String roleName;
    private String description;

    public RoleDTO(Long id, String roleName, String description) {
        this.id = id;
        this.roleName = roleName;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getRoleName() { return roleName; }
    public String getDescription() { return description; }
}
