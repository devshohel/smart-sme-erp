package com.sme.erp.auth.service;

import com.sme.erp.auth.dto.PermissionDTO;

import java.util.List;

public interface PermissionService {
    List<PermissionDTO> getAll();
}
