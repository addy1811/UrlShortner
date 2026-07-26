package com.shortner.controller;

import com.shortner.dto.link.CreateLinkRequest;
import com.shortner.dto.link.LinkResponse;
import com.shortner.dto.link.ResolveLinkResponse;
import com.shortner.dto.link.UpdateLinkRequest;
import com.shortner.security.SecurityUtils;
import com.shortner.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
 
@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
public class ShortLinkController {
 
    private final ShortLinkService shortLinkService;
 
    @PostMapping
    public ResponseEntity<LinkResponse> createLink(@Valid @RequestBody CreateLinkRequest request) {
        LinkResponse response = shortLinkService.createLink(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
 
    @GetMapping
    public ResponseEntity<Page<LinkResponse>> listMyLinks(
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(shortLinkService.listLinksForOwner(SecurityUtils.getCurrentUserId(), pageable));
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<LinkResponse> getLink(@PathVariable UUID id) {
        return ResponseEntity.ok(shortLinkService.getLinkForOwner(SecurityUtils.getCurrentUserId(), id));
    }
 
    @PatchMapping("/{id}")
    public ResponseEntity<LinkResponse> updateLink(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateLinkRequest request
    ) {
        return ResponseEntity.ok(shortLinkService.updateLink(SecurityUtils.getCurrentUserId(), id, request));
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLink(@PathVariable UUID id) {
        shortLinkService.deleteLink(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
 
    /**
     * JSON counterpart to RedirectController's /r/{code} - a plain browser
     * navigation can never carry a Bearer token (browsers don't attach custom
     * headers to normal navigation), so PRIVATE/RESTRICTED links can never be
     * resolved that way for a logged-in user. This endpoint is meant to be
     * called by the frontend's own JS (via axios, which does attach the JWT),
     * returning the destination as JSON so the frontend can redirect the
     * browser itself once it has confirmed access. PUBLIC links work through
     * either path; PRIVATE/RESTRICTED links only work through this one.
     */
    @GetMapping("/resolve/{code}")
    public ResponseEntity<ResolveLinkResponse> resolveLink(
        @PathVariable String code,
        HttpServletRequest request
    ) {
        UUID requestingUserId = SecurityUtils.isAuthenticated() ? SecurityUtils.getCurrentUserId() : null;
        String destinationUrl = shortLinkService.resolveForRedirect(code, requestingUserId, extractClientIp(request));
        return ResponseEntity.ok(new ResolveLinkResponse(destinationUrl));
    }
 
    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
 