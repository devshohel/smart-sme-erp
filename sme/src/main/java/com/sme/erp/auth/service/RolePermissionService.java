package com.sme.erp.auth.service;

import com.sme.erp.auth.dto.PermissionDTO;

import java.util.List;

public interface RolePermissionService {
    List<PermissionDTO> getRolePermissions(Long roleId);
    List<PermissionDTO> updateRolePermissions(Long roleId, List<Long> permissionIds);
}
