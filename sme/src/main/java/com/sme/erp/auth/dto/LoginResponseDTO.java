package com.sme.erp.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LoginResponseDTO {
    private String accessToken;
    private String username;
    private String role;
    private List<String> permissions;
    private LocalDateTime loginTimestamp;

    public LoginResponseDTO(String accessToken, String username, String role, List<String> permissions, LocalDateTime loginTimestamp) {
        this.accessToken = accessToken;
        this.username = username;
        this.role = role;
        this.permissions = permissions;
        this.loginTimestamp = loginTimestamp;
    }

    public String getAccessToken() { return accessToken; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public List<String> getPermissions() { return permissions; }
    public LocalDateTime getLoginTimestamp() { return loginTimestamp; }
}
