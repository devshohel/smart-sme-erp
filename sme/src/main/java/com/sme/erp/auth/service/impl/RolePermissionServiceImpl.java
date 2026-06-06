package com.sme.erp.auth.service.impl;

import com.sme.erp.auth.dto.PermissionDTO;
import com.sme.erp.auth.entity.Permission;
import com.sme.erp.auth.entity.Role;
import com.sme.erp.auth.entity.RolePermission;
import com.sme.erp.auth.repository.PermissionRepository;
import com.sme.erp.auth.repository.RolePermissionRepository;
import com.sme.erp.auth.repository.RoleRepository;
import com.sme.erp.auth.service.RolePermissionService;
import com.sme.erp.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RolePermissionServiceImpl(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getRolePermissions(Long roleId) {
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        return rolePermissionRepository.findWithPermissionByRoleId(roleId)
                .stream()
                .map(RolePermission::getPermission)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PermissionDTO> updateRolePermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        rolePermissionRepository.deleteByRoleId(roleId);
        permissions.forEach(permission -> {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        });

        return getRolePermissions(roleId);
    }

    private PermissionDTO toDto(Permission permission) {
        return new PermissionDTO(permission.getId(), permission.getName(), permission.getModule(), permission.getAction(), permission.getDescription());
    }
}
