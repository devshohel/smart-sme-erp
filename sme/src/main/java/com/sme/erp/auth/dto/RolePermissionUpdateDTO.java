package com.sme.erp.auth.dto;

import java.util.ArrayList;
import java.util.List;

public class RolePermissionUpdateDTO {
    private List<Long> permissionIds = new ArrayList<>();

    public List<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(List<Long> permissionIds) { this.permissionIds = permissionIds; }
}
