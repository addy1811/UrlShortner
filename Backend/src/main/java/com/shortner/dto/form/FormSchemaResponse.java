package com.shortner.dto.form;

import com.shortner.entity.FieldType;

import java.util.List;
import java.util.UUID;

public record FormSchemaResponse(
    UUID linkId,
    List<Field> fields
) {
    public record Field(
        UUID id,
        String fieldKey,
        String label,
        FieldType fieldType,
        boolean required,
        List<String> options,
        int displayOrder
    ) {}
}