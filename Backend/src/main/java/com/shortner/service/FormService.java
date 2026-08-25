package com.shortner.service;

import com.shortner.dto.form.FormFieldRequest;
import com.shortner.dto.form.FormSchemaResponse;
import com.shortner.dto.form.FormSubmissionRequest;
import com.shortner.entity.FieldType;
import com.shortner.entity.FormField;
import com.shortner.dto.form.FormResponseDto;
import com.shortner.entity.FormResponse;
import com.shortner.entity.ShortLink;
import com.shortner.entity.User;
import com.shortner.exception.LinkNotFoundException;
import com.shortner.repository.FormFieldRepository;
import com.shortner.repository.FormResponseRepository;
import com.shortner.repository.ShortLinkRepository;
import com.shortner.repository.UserRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormService {

    private final FormFieldRepository formFieldRepository;
    private final FormResponseRepository formResponseRepository;
    private final ShortLinkRepository shortLinkRepository;
    private final UserRepository userRepository;
    private final AccessControlService accessControlService;

    @Transactional
    public FormSchemaResponse defineFormFields(UUID ownerId, UUID linkId, List<FormFieldRequest> fieldRequests) {
        ShortLink link = shortLinkRepository.findByIdAndOwnerId(linkId, ownerId)
            .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        for (FormFieldRequest req : fieldRequests) {
            boolean needsOptions = req.fieldType() == FieldType.DROPDOWN || req.fieldType() == FieldType.CHECKBOX;
            if (needsOptions && (req.options() == null || req.options().isEmpty())) {
                throw new ValidationException(
                    "Field '" + req.fieldKey() + "' of type " + req.fieldType() + " requires at least one option"
                );
            }
        }
        formFieldRepository.deleteByLinkId(linkId);

        List<FormField> fields = fieldRequests.stream()
            .map(req -> FormField.builder()
                .link(link)
                .fieldKey(req.fieldKey())
                .label(req.label())
                .fieldType(req.fieldType())
                .required(req.required())
                .options(req.options())
                .displayOrder(req.displayOrder())
                .build())
            .toList();

        List<FormField> saved = formFieldRepository.saveAll(fields);
        return toSchemaResponse(linkId, saved);
    }

    @Transactional(readOnly = true)
    public FormSchemaResponse getFormSchema(UUID linkId, UUID requestingUserId) {
        ShortLink link = shortLinkRepository.findById(linkId)
            .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        accessControlService.assertAccessAllowed(link, requestingUserId);

        List<FormField> fields = formFieldRepository.findByLinkIdOrderByDisplayOrderAsc(linkId);
        return toSchemaResponse(linkId, fields);
    }

    @Transactional
    public void submitForm(UUID linkId, UUID submittedByUserId, FormSubmissionRequest request) {
        ShortLink link = shortLinkRepository.findById(linkId)
            .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        accessControlService.assertAccessAllowed(link, submittedByUserId);

        List<FormField> fields = formFieldRepository.findByLinkIdOrderByDisplayOrderAsc(linkId);
        validateSubmission(fields, request.responseData());

        User submittedBy = submittedByUserId != null ? userRepository.getReferenceById(submittedByUserId) : null;

        FormResponse response = FormResponse.builder()
            .link(link)
            .submittedBy(submittedBy)
            .responseData(request.responseData())
            .build();

        formResponseRepository.save(response);
    }

    @Transactional(readOnly = true)
   public Page<FormResponseDto> getResponsesForOwner(UUID ownerId, UUID linkId, Pageable pageable) {
    shortLinkRepository.findByIdAndOwnerId(linkId, ownerId)
        .orElseThrow(() -> new LinkNotFoundException("Link not found"));

    return formResponseRepository.findByLinkIdOrderBySubmittedAtDesc(linkId, pageable)
        .map(r -> new FormResponseDto(
            r.getId(),
            r.getSubmittedBy() != null ? r.getSubmittedBy().getId() : null,
            r.getSubmittedBy() != null ? r.getSubmittedBy().getUsername() : null,
            r.getResponseData(),
            r.getSubmittedAt()
        ));
}

private static final java.util.regex.Pattern EMAIL_PATTERN =
    java.util.regex.Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

private void validateSubmission(List<FormField> fields, Map<String, Object> responseData) {
    for (FormField field : fields) {
        Object value = responseData.get(field.getFieldKey());

        if (field.isRequired() && (value == null || value.toString().isBlank())) {
            throw new ValidationException("Field '" + field.getFieldKey() + "' is required");
        }

        if (value == null || value.toString().isBlank()) {
            continue;
        }

        String stringValue = value.toString();

        switch (field.getFieldType()) {
            case NUMBER -> {
                try {
                    Double.parseDouble(stringValue);
                } catch (NumberFormatException e) {
                    throw new ValidationException(
                        "Field '" + field.getFieldKey() + "' must be a number"
                    );
                }
            }
            case DATE -> {
                try {
                    java.time.LocalDate.parse(stringValue);
                } catch (java.time.format.DateTimeParseException e) {
                    throw new ValidationException(
                        "Field '" + field.getFieldKey() + "' must be a valid date (YYYY-MM-DD)"
                    );
                }
            }
            case EMAIL -> {
                if (!EMAIL_PATTERN.matcher(stringValue).matches()) {
                    throw new ValidationException(
                        "Field '" + field.getFieldKey() + "' must be a valid email address"
                    );
                }
            }
            case DROPDOWN, CHECKBOX -> {
                if (field.getOptions() != null && !field.getOptions().contains(stringValue)) {
                    throw new ValidationException(
                        "Value '" + value + "' is not a valid option for field '" + field.getFieldKey() + "'"
                    );
                }
            }
            case TEXT -> { }
        }
    }
}
    private FormSchemaResponse toSchemaResponse(UUID linkId, List<FormField> fields) {
        List<FormSchemaResponse.Field> fieldDtos = fields.stream()
            .map(f -> new FormSchemaResponse.Field(
                f.getId(), f.getFieldKey(), f.getLabel(), f.getFieldType(),
                f.isRequired(), f.getOptions(), f.getDisplayOrder()
            ))
            .toList();

        return new FormSchemaResponse(linkId, fieldDtos);
    }
}