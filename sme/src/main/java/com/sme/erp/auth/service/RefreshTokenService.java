package com.sme.erp.auth.service;

import com.sme.erp.auth.entity.RefreshToken;
import com.sme.erp.auth.entity.User;
import com.sme.erp.auth.repository.RefreshTokenRepository;
import com.sme.erp.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final TokenHashService tokenHashService;
    private final long refreshExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository repository,
            TokenHashService tokenHashService,
            @Value("${app.jwt.refresh-expiration-ms:1209600000}") long refreshExpirationMs) {
        this.repository = repository;
        this.tokenHashService = tokenHashService;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public IssuedRefreshToken issue(User user) {
        String rawToken = tokenHashService.newOpaqueToken();
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(tokenHashService.sha256(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        repository.save(token);
        return new IssuedRefreshToken(rawToken, token);
    }

    @Transactional
    public IssuedRefreshToken rotate(String rawRefreshToken) {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken current = repository.findByTokenHash(tokenHashService.sha256(rawRefreshToken))
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (!current.isActive(now)) {
            throw new BadRequestException("Invalid refresh token");
        }
        IssuedRefreshToken replacement = issue(current.getUser());
        current.setRevokedAt(now);
        current.setReplacedByTokenHash(replacement.entity().getTokenHash());
        repository.save(current);
        return replacement;
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        repository.findByTokenHash(tokenHashService.sha256(rawRefreshToken))
                .ifPresent(token -> {
                    if (token.getRevokedAt() == null) {
                        token.setRevokedAt(LocalDateTime.now());
                        repository.save(token);
                    }
                });
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        repository.revokeActiveTokensForUser(userId, LocalDateTime.now());
    }

    public record IssuedRefreshToken(String rawToken, RefreshToken entity) {}
}
