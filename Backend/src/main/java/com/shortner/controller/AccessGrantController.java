package com.shortner.controller;

import com.shortner.dto.link.GrantAccessRequest;
import com.shortner.dto.link.GrantResponse;
import com.shortner.entity.LinkAccessGrant;
import com.shortner.security.SecurityUtils;
import com.shortner.service.AccessControlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/links/{linkId}/grants")
@RequiredArgsConstructor
public class AccessGrantController {

    private final AccessControlService accessControlService;

    @PostMapping
    public ResponseEntity<GrantResponse> grantAccess(
        @PathVariable UUID linkId,
        @Valid @RequestBody GrantAccessRequest request
    ) {
        LinkAccessGrant grant = accessControlService.grantAccess(SecurityUtils.getCurrentUserId(), linkId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(grant));
    }

    @GetMapping
    public ResponseEntity<List<GrantResponse>> listGrants(@PathVariable UUID linkId) {
        List<GrantResponse> grants = accessControlService.listGrants(SecurityUtils.getCurrentUserId(), linkId)
            .stream()
            .map(this::toResponse)
            .toList();
        return ResponseEntity.ok(grants);
    }

    @DeleteMapping("/{grantId}")
    public ResponseEntity<Void> revokeAccess(@PathVariable UUID linkId, @PathVariable UUID grantId) {
        accessControlService.revokeAccess(SecurityUtils.getCurrentUserId(), linkId, grantId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{grantId}/reactivate")
    public ResponseEntity<GrantResponse> reactivateAccess(@PathVariable UUID linkId, @PathVariable UUID grantId) {
    LinkAccessGrant grant = accessControlService.reactivateAccess(SecurityUtils.getCurrentUserId(), linkId, grantId);
    return ResponseEntity.ok(toResponse(grant));
}

    private GrantResponse toResponse(LinkAccessGrant grant) {
        return new GrantResponse(
            grant.getId(),
            grant.getGrantee() != null ? grant.getGrantee().getId() : null,
            grant.getGrantee() != null ? grant.getGrantee().getUsername() : null,
            grant.getInvitedEmail(),
            grant.getStatus(),
            grant.getGrantedAt()
        );
    }
}