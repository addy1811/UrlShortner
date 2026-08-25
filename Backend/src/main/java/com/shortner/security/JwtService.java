package com.shortner.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration-ms}") long accessTokenExpirationMs,
        @Value("${app.jwt.refresh-expiration-ms}") long refreshTokenExpirationMs
    ) {
        // HS256 requires a key >= 256 bits (32 bytes). The dev default in application.yml
        // is exactly 32 chars for this reason - swap it for a real `openssl rand -base64 32`
        // secret before deploying anywhere real.
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(UserPrincipal principal) {
        return buildToken(principal, accessTokenExpirationMs, "access");
    }

    public String generateRefreshToken(UserPrincipal principal) {
        return buildToken(principal, refreshTokenExpirationMs, "refresh");
    }

    public Instant getAccessTokenExpiry() {
        return Instant.now().plusMillis(accessTokenExpirationMs);
    }

    private String buildToken(UserPrincipal principal, long expirationMs, String tokenType) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(principal.getUsername())
            .claim("userId", principal.getId().toString())
            .claim("type", tokenType)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(expirationMs)))
            .signWith(signingKey)
            .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        String userId = parseClaims(token).get("userId", String.class);
        return UUID.fromString(userId);
    }

    public boolean isTokenValid(String token, UserPrincipal principal) {
        try {
            Claims claims = parseClaims(token);
            boolean usernameMatches = claims.getSubject().equals(principal.getUsername());
            boolean notExpired = claims.getExpiration().after(new Date());
            return usernameMatches && notExpired;
        } catch (JwtException | IllegalArgumentException e) {
            // Covers expired, malformed, or tampered tokens - all treated as "not valid"
            // rather than leaking parse-error details to the caller.
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
    try {
        return parseClaims(token).getExpiration().before(new Date());
    } catch (JwtException | IllegalArgumentException e) {
        return true; // malformed/tampered treated as expired/invalid
    }
}

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get("type", String.class));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}