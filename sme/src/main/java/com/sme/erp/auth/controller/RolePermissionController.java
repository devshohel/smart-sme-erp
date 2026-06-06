package com.sme.erp.auth.controller;

import com.sme.erp.auth.dto.PermissionDTO;
import com.sme.erp.auth.dto.RolePermissionUpdateDTO;
import com.sme.erp.auth.service.RolePermissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@CrossOrigin(origins = "*")
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;

    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<List<PermissionDTO>> getRolePermissions(@PathVariable Long roleId) {
        return ResponseEntity.ok(rolePermissionService.getRolePermissions(roleId));
    }

    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ROLE_EDIT')")
    public ResponseEntity<List<PermissionDTO>> updateRolePermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody RolePermissionUpdateDTO dto) {
        return ResponseEntity.ok(rolePermissionService.updateRolePermissions(roleId, dto.getPermissionIds()));
    }
}
