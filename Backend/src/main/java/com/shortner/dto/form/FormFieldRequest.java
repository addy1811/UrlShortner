package com.shortner.dto.form;

import com.shortner.entity.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record FormFieldRequest(

    // Machine key used later as the JSON key in submitted responses, e.g. "phoneNumber".
    @NotBlank(message = "Field key is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{0,49}$", message = "Field key must start with a letter and be alphanumeric/underscore")
    String fieldKey,

    @NotBlank(message = "Label is required")
    String label,

    @NotNull(message = "Field type is required")
    FieldType fieldType,

    boolean required,

    // Required (non-empty) only when fieldType is DROPDOWN or CHECKBOX - validated in FormService.
    List<String> options,

    int displayOrder

) {}