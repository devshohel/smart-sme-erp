package com.sme.erp.auth.dto;

import java.time.LocalDateTime;

public class LoginResponseDTO {
    private String accessToken;
    private String username;
    private String role;
    private LocalDateTime loginTimestamp;

    public LoginResponseDTO(String accessToken, String username, String role, LocalDateTime loginTimestamp) {
        this.accessToken = accessToken;
        this.username = username;
        this.role = role;
        this.loginTimestamp = loginTimestamp;
    }

    public String getAccessToken() { return accessToken; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public LocalDateTime getLoginTimestamp() { return loginTimestamp; }
}
