package com.shortner.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Central place every controller/service goes through to answer "who is making
 * this request" - avoids scattering SecurityContextHolder.getContext() calls
 * (and inconsistent casting) throughout the codebase.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        return principal;
    }

    public static UUID getCurrentUserId() {
        return getCurrentPrincipal().getId();
    }

    public static String getCurrentUsername() {
        return getCurrentPrincipal().getUsername();
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
            && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof UserPrincipal;
    }
}