package com.sme.erp.auth.service;

import com.sme.erp.auth.dto.LoginRequestDTO;
import com.sme.erp.auth.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
}
