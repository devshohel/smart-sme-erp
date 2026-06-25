package com.sme.erp.auth.service;

import com.sme.erp.auth.dto.ChangePasswordRequestDTO;
import com.sme.erp.auth.dto.LoginRequestDTO;
import com.sme.erp.auth.dto.LoginResponseDTO;
import com.sme.erp.auth.dto.LogoutRequestDTO;
import com.sme.erp.auth.dto.RefreshTokenRequestDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
    LoginResponseDTO refresh(RefreshTokenRequestDTO request);
    void logout(LogoutRequestDTO request, String authorizationHeader);
    void changePassword(ChangePasswordRequestDTO request);
}
