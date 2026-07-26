package com.shortner.dto.link;

import com.shortner.entity.Visibility;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

/**
 * All fields nullable by design - PATCH semantics. Only non-null fields are
 * applied by ShortLinkService; the rest keep their existing DB values.
 */
public record UpdateLinkRequest(

    @Pattern(regexp = "^https?://.+", message = "Destination URL must start with http:// or https://")
    @Size(max = 2048, message = "URL is too long")
    String destinationUrl,

    Visibility visibility,

    @Future(message = "Expiry must be in the future")
    Instant expiresAt,

    @Positive(message = "Max uses must be positive")
    Integer maxUses,

    Boolean active,

    Map<String, Object> metadata

) {}