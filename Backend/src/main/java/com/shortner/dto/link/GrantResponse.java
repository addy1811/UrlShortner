package com.shortner.dto.link;
 
import com.shortner.entity.GrantStatus;
 
import java.time.Instant;
import java.util.UUID;
 
public record GrantResponse(
    UUID id,
    UUID granteeUserId,
    String granteeUsername,
    String invitedEmail,
    GrantStatus status,
    Instant grantedAt
) {}
 