package com.sme.erp.auth.dto;

public class PermissionDTO {
    private Long id;
    private String name;
    private String module;
    private String action;
    private String description;

    public PermissionDTO(Long id, String name, String module, String action, String description) {
        this.id = id;
        this.name = name;
        this.module = module;
        this.action = action;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getModule() { return module; }
    public String getAction() { return action; }
    public String getDescription() { return description; }
}
