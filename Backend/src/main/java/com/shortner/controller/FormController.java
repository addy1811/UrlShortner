package com.shortner.controller;

import com.shortner.dto.form.FormFieldRequest;
import com.shortner.dto.form.FormSchemaResponse;
import com.shortner.dto.form.FormSubmissionRequest;
import com.shortner.entity.FormResponse;
import com.shortner.dto.form.FormResponseDto;
import com.shortner.security.SecurityUtils;
import com.shortner.service.FormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/links/{linkId}/form")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    /** Owner-only: replaces the full set of fields on this link's data-collection form. */
    @PostMapping
    public ResponseEntity<FormSchemaResponse> defineFields(
        @PathVariable UUID linkId,
        @Valid @RequestBody List<FormFieldRequest> fields
    ) {
        FormSchemaResponse response = formService.defineFormFields(SecurityUtils.getCurrentUserId(), linkId, fields);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Public: visitors need to see the form schema in order to render/fill it out. */
    @GetMapping
    public ResponseEntity<FormSchemaResponse> getSchema(@PathVariable UUID linkId) {
        UUID requestingUserId = SecurityUtils.isAuthenticated() ? SecurityUtils.getCurrentUserId() : null;
        return ResponseEntity.ok(formService.getFormSchema(linkId, requestingUserId));
    }

    /** Public: anonymous submissions are allowed unless the link's visibility requires auth. */
    @PostMapping("/submit")
    public ResponseEntity<Void> submit(
        @PathVariable UUID linkId,
        @Valid @RequestBody FormSubmissionRequest request
    ) {
        UUID submittedByUserId = SecurityUtils.isAuthenticated() ? SecurityUtils.getCurrentUserId() : null;
        formService.submitForm(linkId, submittedByUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** Owner-only: paginated view of everything submitted through this link's form. */
    @GetMapping("/responses")
    public ResponseEntity<Page<FormResponseDto>> getResponses(
        @PathVariable UUID linkId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<FormResponseDto> responses = formService.getResponsesForOwner(
            SecurityUtils.getCurrentUserId(), linkId, pageable
        );
        return ResponseEntity.ok(responses);
    }
}