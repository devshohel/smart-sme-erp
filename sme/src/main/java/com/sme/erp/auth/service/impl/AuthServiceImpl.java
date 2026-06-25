package com.sme.erp.auth.service.impl;

import com.sme.erp.auth.dto.ChangePasswordRequestDTO;
import com.sme.erp.auth.dto.LoginRequestDTO;
import com.sme.erp.auth.dto.LoginResponseDTO;
import com.sme.erp.auth.dto.LogoutRequestDTO;
import com.sme.erp.auth.dto.RefreshTokenRequestDTO;
import com.sme.erp.auth.entity.User;
import com.sme.erp.auth.repository.RolePermissionRepository;
import com.sme.erp.auth.repository.UserRepository;
import com.sme.erp.auth.security.JwtService;
import com.sme.erp.auth.service.AccessTokenBlacklistService;
import com.sme.erp.auth.service.AuthService;
import com.sme.erp.auth.service.RefreshTokenService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.LoginHistoryService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.enums.Status;
import org.springframework.beans.factory.annotation.Value;
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
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenBlacklistService accessTokenBlacklistService;
    private final LoginHistoryService loginHistoryService;
    private final ActivityLogService activityLogService;
    private final int maxFailedAttempts;
    private final long lockDurationMinutes;

    public AuthServiceImpl(
            UserRepository userRepository,
            RolePermissionRepository rolePermissionRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            AccessTokenBlacklistService accessTokenBlacklistService,
            LoginHistoryService loginHistoryService,
            ActivityLogService activityLogService,
            @Value("${app.security.max-failed-login-attempts:5}") int maxFailedAttempts,
            @Value("${app.security.lock-duration-minutes:15}") long lockDurationMinutes) {
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.accessTokenBlacklistService = accessTokenBlacklistService;
        this.loginHistoryService = loginHistoryService;
        this.activityLogService = activityLogService;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockDurationMinutes = lockDurationMinutes;
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        if (user == null) {
            loginHistoryService.failed(request.getUsername(), "Invalid username or password");
            activityLogService.log("LOGIN_FAILED", "AUTH", "users", null, "Failed login for unknown username: " + request.getUsername());
            throw new BadRequestException("Invalid username or password");
        }
        if (isLocked(user)) {
            loginHistoryService.failed(request.getUsername(), "Account is temporarily locked");
            activityLogService.log("LOGIN_BLOCKED", "AUTH", "users", user.getId(), "Login blocked because account is locked");
            throw new BadRequestException("Account is temporarily locked. Try again later.");
        }
        if (user.getStatus() != Status.ACTIVE || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            registerFailedLogin(user);
            loginHistoryService.failed(request.getUsername(), "Invalid username or password");
            throw new BadRequestException("Invalid username or password");
        }

        LocalDateTime loginTime = LocalDateTime.now();
        user.setLastLogin(loginTime);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        loginHistoryService.success(user);
        activityLogService.log("LOGIN_SUCCESS", "AUTH", "users", user.getId(), "User logged in");
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user);

        return new LoginResponseDTO(
                jwtService.generateToken(user),
                refreshToken.rawToken(),
                jwtService.getExpirationSeconds(),
                user.getUsername(),
                user.getName(),
                user.getRole().getRoleName(),
                rolePermissionRepository.findPermissionNamesByRoleId(user.getRole().getId()),
                loginTime);
    }

    @Override
    @Transactional
    public LoginResponseDTO refresh(RefreshTokenRequestDTO request) {
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.rotate(request.getRefreshToken());
        User user = refreshToken.entity().getUser();
        if (user.getStatus() != Status.ACTIVE || isLocked(user)) {
            refreshTokenService.revokeAllForUser(user.getId());
            activityLogService.log("REFRESH_DENIED", "AUTH", "users", user.getId(), "Refresh token denied for inactive or locked user");
            throw new BadRequestException("User session is no longer valid");
        }
        activityLogService.log("REFRESH_TOKEN", "AUTH", "users", user.getId(), "Refresh token rotated");
        return new LoginResponseDTO(
                jwtService.generateToken(user),
                refreshToken.rawToken(),
                jwtService.getExpirationSeconds(),
                user.getUsername(),
                user.getName(),
                user.getRole().getRoleName(),
                rolePermissionRepository.findPermissionNamesByRoleId(user.getRole().getId()),
                LocalDateTime.now());
    }

    @Override
    @Transactional
    public void logout(LogoutRequestDTO request, String authorizationHeader) {
        if (request != null) {
            refreshTokenService.revoke(request.getRefreshToken());
        }
        String accessToken = extractBearerToken(authorizationHeader);
        accessTokenBlacklistService.blacklist(accessToken);
        activityLogService.log("LOGOUT", "AUTH", "users", currentUserIdOrNull(), "User logged out and session tokens were revoked");
        SecurityContextHolder.clearContext();
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
        user.setTokenVersion((user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1);
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user.getId());
        activityLogService.log("CHANGE_PASSWORD", "AUTH", "users", user.getId(), "User changed password; active refresh tokens revoked");
    }

    private boolean isLocked(User user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
    }

    private void registerFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        attempts++;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
            activityLogService.log("ACCOUNT_LOCKED", "AUTH", "users", user.getId(), "Account locked after failed login attempts");
        } else {
            activityLogService.log("LOGIN_FAILED", "AUTH", "users", user.getId(), "Failed login attempt " + attempts);
        }
        userRepository.save(user);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7);
    }

    private Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).map(User::getId).orElse(null);
    }
}
