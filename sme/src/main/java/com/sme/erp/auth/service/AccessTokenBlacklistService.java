package com.sme.erp.auth.service;

import com.sme.erp.auth.entity.BlacklistedAccessToken;
import com.sme.erp.auth.repository.BlacklistedAccessTokenRepository;
import com.sme.erp.auth.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccessTokenBlacklistService {
    private final BlacklistedAccessTokenRepository repository;
    private final TokenHashService tokenHashService;
    private final JwtService jwtService;

    public AccessTokenBlacklistService(
            BlacklistedAccessTokenRepository repository,
            TokenHashService tokenHashService,
            JwtService jwtService) {
        this.repository = repository;
        this.tokenHashService = tokenHashService;
        this.jwtService = jwtService;
    }

    public boolean isBlacklisted(String token) {
        return repository.existsByTokenHashAndExpiresAtAfter(tokenHashService.sha256(token), LocalDateTime.now());
    }

    @Transactional
    public void blacklist(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        LocalDateTime expiresAt = jwtService.extractExpirationTime(token);
        if (!expiresAt.isAfter(LocalDateTime.now())) {
            return;
        }
        String hash = tokenHashService.sha256(token);
        if (repository.existsByTokenHashAndExpiresAtAfter(hash, LocalDateTime.now())) {
            return;
        }
        BlacklistedAccessToken blacklisted = new BlacklistedAccessToken();
        blacklisted.setTokenHash(hash);
        blacklisted.setExpiresAt(expiresAt);
        repository.save(blacklisted);
        repository.deleteExpired(LocalDateTime.now());
    }
}
