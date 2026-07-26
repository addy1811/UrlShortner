package com.shortner.controller;


import com.shortner.security.SecurityUtils;
import com.shortner.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final ShortLinkService shortLinkService;

    @GetMapping("/r/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
        // Anonymous requests (no valid JWT) get null here - assertAccessAllowed treats
        // that as "unauthenticated visitor" and applies PUBLIC/PRIVATE/RESTRICTED rules accordingly.
        UUID requestingUserId = SecurityUtils.isAuthenticated() ? SecurityUtils.getCurrentUserId() : null;

        String destinationUrl = shortLinkService.resolveForRedirect(code, requestingUserId, extractClientIp(request));

        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, destinationUrl)
            .build();
    }

    /**
     * Prefers X-Forwarded-For (set by load balancers/reverse proxies in real deployments)
     * over getRemoteAddr(), which would just report the proxy's own IP otherwise.
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}