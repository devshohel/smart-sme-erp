package com.sme.erp.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresInSeconds;
    private String username;
    private String name;
    private String role;
    private List<String> permissions;
    private LocalDateTime loginTimestamp;

    public LoginResponseDTO(String accessToken, String username, String name, String role, List<String> permissions, LocalDateTime loginTimestamp) {
        this(accessToken, null, 0, username, name, role, permissions, loginTimestamp);
    }

    public LoginResponseDTO(String accessToken, String refreshToken, long expiresInSeconds, String username, String name, String role, List<String> permissions, LocalDateTime loginTimestamp) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresInSeconds = expiresInSeconds;
        this.username = username;
        this.name = name;
        this.role = role;
        this.permissions = permissions;
        this.loginTimestamp = loginTimestamp;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public List<String> getPermissions() { return permissions; }
    public LocalDateTime getLoginTimestamp() { return loginTimestamp; }
}
