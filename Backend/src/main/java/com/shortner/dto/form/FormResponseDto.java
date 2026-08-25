package com.shortner.dto.form;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record FormResponseDto(
    UUID id,
    UUID submittedByUserId,
    String submittedByUsername,
    Map<String, Object> responseData,
    Instant submittedAt
) {}