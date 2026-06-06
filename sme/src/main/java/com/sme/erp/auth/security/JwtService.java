package com.sme.erp.auth.security;

import com.sme.erp.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret:${app.jwt.secret:smart-sme-erp-phase-one-jwt-secret-key-2026}}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        byte[] keyBytes = resolveKeyBytes(secret);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes for HS256");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole().getRoleName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token, String username) {
        Claims claims = claims(token);
        return claims.getSubject().equals(username) && claims.getExpiration().after(new Date());
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private byte[] resolveKeyBytes(String secret) {
        String normalizedSecret = secret == null ? "" : secret.trim();
        if (isBase64Secret(normalizedSecret)) {
            return Decoders.BASE64.decode(normalizedSecret);
        }
        return normalizedSecret.getBytes(StandardCharsets.UTF_8);
    }

    private boolean isBase64Secret(String secret) {
        return secret.length() % 4 == 0 && secret.matches("^[A-Za-z0-9+/]+={0,2}$");
    }
}
