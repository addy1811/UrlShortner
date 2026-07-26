package com.shortner.dto.auth;

import java.time.Instant;
import java.util.UUID;
 
public record AuthResponse(
    String accessToken,
    String refreshToken,
    Instant accessTokenExpiresAt,
    UUID userId,
    String username
) {}
 