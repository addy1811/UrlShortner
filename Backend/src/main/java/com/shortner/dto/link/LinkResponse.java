package com.shortner.dto.link;
 
import com.shortner.entity.Visibility;
 
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
 
/**
 * Deliberately excludes encryptedDestination/encryptionIv - those never leave the server.
 * destinationUrl below is the *decrypted* value, only ever included when the
 * requester is verified as the owner (see ShortLinkService).
 */
public record LinkResponse(
    UUID id,
    String shortCode,
    String shortUrl,           // fully-formed e.g. http://localhost:8080/r/aB3xQ9kL
    String destinationUrl,     // omitted (null) in list views for non-owners
    Visibility visibility,
    String customAlias,
    Instant expiresAt,
    Integer maxUses,
    int useCount,
    boolean active,
    Map<String, Object> metadata,
    Instant createdAt
) {}
 