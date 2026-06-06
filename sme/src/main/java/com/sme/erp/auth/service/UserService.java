package com.sme.erp.auth.service;

import com.sme.erp.auth.dto.RoleDTO;
import com.sme.erp.auth.dto.UserDTO;
import com.sme.erp.enums.Status;

import java.util.List;

public interface UserService {
    List<UserDTO> getAll(String keyword, Status status);
    UserDTO getById(Long id);
    UserDTO create(UserDTO dto);
    UserDTO update(Long id, UserDTO dto);
    UserDTO deactivate(Long id);
    void delete(Long id);
    List<RoleDTO> getRoles();
}
