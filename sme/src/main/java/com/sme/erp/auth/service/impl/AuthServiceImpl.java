package com.sme.erp.auth.service.impl;

import com.sme.erp.auth.dto.ChangePasswordRequestDTO;
import com.sme.erp.auth.dto.LoginRequestDTO;
import com.sme.erp.auth.dto.LoginResponseDTO;
import com.sme.erp.auth.entity.User;
import com.sme.erp.auth.repository.RolePermissionRepository;
import com.sme.erp.auth.repository.UserRepository;
import com.sme.erp.auth.security.JwtService;
import com.sme.erp.auth.service.AuthService;
import com.sme.erp.audit.service.LoginHistoryService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.enums.Status;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginHistoryService loginHistoryService;

    public AuthServiceImpl(
            UserRepository userRepository,
            RolePermissionRepository rolePermissionRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginHistoryService loginHistoryService) {
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginHistoryService = loginHistoryService;
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        if (user == null) {
            loginHistoryService.failed(request.getUsername(), "Invalid username or password");
            throw new BadRequestException("Invalid username or password");
        }
        if (user.getStatus() != Status.ACTIVE || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginHistoryService.failed(request.getUsername(), "Invalid username or password");
            throw new BadRequestException("Invalid username or password");
        }

        LocalDateTime loginTime = LocalDateTime.now();
        user.setLastLogin(loginTime);
        userRepository.save(user);
        loginHistoryService.success(user);

        return new LoginResponseDTO(
                jwtService.generateToken(user),
                user.getUsername(),
                user.getRole().getRoleName(),
                rolePermissionRepository.findPermissionNamesByRoleId(user.getRole().getId()),
                loginTime);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequestDTO request) {
        if (request.getNewPassword() == null || !request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new BadRequestException("User is not authenticated");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
