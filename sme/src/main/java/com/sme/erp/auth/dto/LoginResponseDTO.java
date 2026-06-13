package com.sme.erp.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LoginResponseDTO {
    private String accessToken;
    private String username;
    private String name;
    private String role;
    private List<String> permissions;
    private LocalDateTime loginTimestamp;

    public LoginResponseDTO(String accessToken, String username, String name, String role, List<String> permissions, LocalDateTime loginTimestamp) {
        this.accessToken = accessToken;
        this.username = username;
        this.name = name;
        this.role = role;
        this.permissions = permissions;
        this.loginTimestamp = loginTimestamp;
    }

    public String getAccessToken() { return accessToken; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public List<String> getPermissions() { return permissions; }
    public LocalDateTime getLoginTimestamp() { return loginTimestamp; }
}
