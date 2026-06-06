package com.sme.erp.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final String[] ERP_ROLES = {"ADMIN", "MANAGER", "STAFF"};

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login").permitAll()
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
                                "/api/v1/audit", "/api/v1/audit/**")
                        .hasAnyRole(ERP_ROLES)
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
