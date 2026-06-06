package com.sme.erp.auth.controller;

import com.sme.erp.auth.dto.LoginRequestDTO;
import com.sme.erp.auth.dto.LoginResponseDTO;
import com.sme.erp.auth.service.AuthService;
import com.sme.erp.audit.service.ActivityLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;
    private final ActivityLogService activityLogService;

    public AuthController(AuthService authService, ActivityLogService activityLogService) {
        this.authService = authService;
        this.activityLogService = activityLogService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout() {
        activityLogService.log("LOGOUT", "AUTH", "users", null, "User logged out");
        return ResponseEntity.noContent().build();
    }
}
