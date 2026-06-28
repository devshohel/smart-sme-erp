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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMs;
    private final long allowedClockSkewSeconds;

    public JwtService(
            @Value("${app.jwt.secret:${JWT_SECRET:}}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMs,
            @Value("${app.jwt.clock-skew-seconds:60}") long allowedClockSkewSeconds) {
        byte[] keyBytes = resolveKeyBytes(secret);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET or app.jwt.secret must be at least 32 bytes for HS256");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        this.allowedClockSkewSeconds = allowedClockSkewSeconds;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole().getRoleName())
                .claim("tokenVersion", user.getTokenVersion() == null ? 0 : user.getTokenVersion())
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

    public int extractTokenVersion(String token) {
        Object value = claims(token).get("tokenVersion");
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    public LocalDateTime extractExpirationTime(String token) {
        Date expiration = claims(token).getExpiration();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(expiration.getTime()), ZoneId.systemDefault());
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .clockSkewSeconds(allowedClockSkewSeconds)
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
