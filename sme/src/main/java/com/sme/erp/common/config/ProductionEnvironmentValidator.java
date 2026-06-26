package com.sme.erp.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("prod")
public class ProductionEnvironmentValidator {
    private static final List<String> REQUIRED_PROPERTIES = List.of(
            "JWT_SECRET",
            "DB_URL",
            "DB_USERNAME",
            "DB_PASSWORD",
            "CORS_ALLOWED_ORIGINS",
            "SERVER_PORT");

    private final Environment environment;

    public ProductionEnvironmentValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validate() {
        for (String property : REQUIRED_PROPERTIES) {
            String value = environment.getProperty(property);
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Missing required production environment variable: " + property);
            }
        }

        String jwtSecret = environment.getProperty("JWT_SECRET", "");
        if (jwtSecret.trim().getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes for production");
        }

        String origins = environment.getProperty("CORS_ALLOWED_ORIGINS", "");
        if (origins.contains("*") || origins.contains("localhost") || origins.contains("127.0.0.1")) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS must contain explicit production origins only");
        }

        String serverPort = environment.getProperty("SERVER_PORT", "");
        try {
            int port = Integer.parseInt(serverPort);
            if (port <= 0 || port > 65535) {
                throw new IllegalStateException("SERVER_PORT must be between 1 and 65535");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("SERVER_PORT must be numeric", ex);
        }
    }
}
