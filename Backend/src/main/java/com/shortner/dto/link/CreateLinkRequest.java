package com.shortner.dto.link;

import com.shortner.entity.Visibility;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.Map;

/**
 * This is "the form" the owner fills out to generate a link - every field
 * except destinationUrl is optional, matching the "user chooses what to include" requirement.
 */
public record CreateLinkRequest(

    @NotBlank(message = "Destination URL is required")
    @Pattern(regexp = "^https?://.+", message = "Destination URL must start with http:// or https://")
    @Size(max = 2048, message = "URL is too long")
    String destinationUrl,

    // Defaults to PRIVATE in the service layer if omitted.
    Visibility visibility,

    // Optional vanity alias, e.g. "my-portfolio" -> short.ly/my-portfolio
    @Pattern(regexp = "^[a-zA-Z0-9-_]{3,50}$", message = "Alias must be 3-50 alphanumeric/hyphen characters")
    String customAlias,

    // Null = never expires
    @Future(message = "Expiry must be in the future")
    Instant expiresAt,

    // Null = unlimited uses
    @Positive(message = "Max uses must be positive")
    Integer maxUses,

    // Freeform owner metadata - description, tags, etc. Stored as-is in the JSONB column.
    Map<String, Object> metadata

) {}