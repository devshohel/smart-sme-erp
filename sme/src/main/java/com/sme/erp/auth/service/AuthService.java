package com.sme.erp.auth.service;

import com.sme.erp.auth.dto.ChangePasswordRequestDTO;
import com.sme.erp.auth.dto.LoginRequestDTO;
import com.sme.erp.auth.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
    void changePassword(ChangePasswordRequestDTO request);
}
