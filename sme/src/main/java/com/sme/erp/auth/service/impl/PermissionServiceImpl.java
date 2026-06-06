package com.sme.erp.auth.service.impl;

import com.sme.erp.auth.dto.PermissionDTO;
import com.sme.erp.auth.entity.Permission;
import com.sme.erp.auth.repository.PermissionRepository;
import com.sme.erp.auth.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getAll() {
        return permissionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Permission::getModule).thenComparing(Permission::getAction))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private PermissionDTO toDto(Permission permission) {
        return new PermissionDTO(
                permission.getId(),
                permission.getName(),
                permission.getModule(),
                permission.getAction(),
                permission.getDescription());
    }
}
