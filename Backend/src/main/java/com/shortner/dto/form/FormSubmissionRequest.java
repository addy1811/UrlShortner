package com.shortner.dto.form;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

/**
 * responseData is keyed by FormField.fieldKey, e.g. {"phoneNumber": "555-1234"}.
 * FormService cross-checks the keys/types against the link's FormField definitions
 * before persisting - required fields must be present, DROPDOWN/CHECKBOX values
 * must match the defined options.
 */
public record FormSubmissionRequest(

    @NotEmpty(message = "Response data cannot be empty")
    Map<String, Object> responseData

) {}