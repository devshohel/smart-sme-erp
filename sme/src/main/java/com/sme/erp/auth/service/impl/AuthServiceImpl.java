package com.sme.erp.auth.service.impl;

import com.sme.erp.auth.dto.LoginRequestDTO;
import com.sme.erp.auth.dto.LoginResponseDTO;
import com.sme.erp.auth.entity.User;
import com.sme.erp.auth.repository.UserRepository;
import com.sme.erp.auth.security.JwtService;
import com.sme.erp.auth.service.AuthService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.enums.Status;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));
        if (user.getStatus() != Status.ACTIVE || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }

        LocalDateTime loginTime = LocalDateTime.now();
        user.setLastLogin(loginTime);
        userRepository.save(user);

        return new LoginResponseDTO(
                jwtService.generateToken(user),
                user.getUsername(),
                user.getRole().getRoleName(),
                loginTime);
    }
}
