package com.sme.erp.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final String[] ERP_ROLES = {"SUPER_ADMIN", "ADMIN", "MANAGER", "STAFF", "AUDITOR"};

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${app.cors.allowed-origins:http://localhost:4200}") String allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .headers(headers -> headers
                        .contentTypeOptions(contentTypeOptions -> {})
                        .frameOptions(frameOptions -> frameOptions.deny()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/files/products/**").permitAll()
                        .requestMatchers(
                                "/api/v1/dashboard", "/api/v1/dashboard/**",
                                "/api/v1/products", "/api/v1/products/**",
                                "/api/v1/brands", "/api/v1/brands/**",
                                "/api/v1/categories", "/api/v1/categories/**",
                                "/api/v1/uoms", "/api/v1/uoms/**",
                                "/api/v1/customers", "/api/v1/customers/**",
                                "/api/v1/suppliers", "/api/v1/suppliers/**",
                                "/api/v1/inventory", "/api/v1/inventory/**",
                                "/api/v1/stocks", "/api/v1/stocks/**",
                                "/api/v1/warehouses", "/api/v1/warehouses/**",
                                "/api/v1/movements", "/api/v1/movements/**",
                                "/api/v1/adjustments", "/api/v1/adjustments/**",
                                "/api/v1/purchases", "/api/v1/purchases/**",
                                "/api/v1/sales", "/api/v1/sales/**",
                                "/api/v1/reports", "/api/v1/reports/**",
                                "/api/v1/users", "/api/v1/users/**",
                                "/api/v1/permissions", "/api/v1/permissions/**",
                                "/api/v1/roles", "/api/v1/roles/**",
                                "/api/v1/audit", "/api/v1/audit/**",
                                "/api/v1/settings", "/api/v1/settings/**")
                        .hasAnyRole(ERP_ROLES)
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(parseCsv(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "X-Request-ID"));
        configuration.setExposedHeaders(List.of("Authorization", "X-Request-ID"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> parseCsv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }
}
