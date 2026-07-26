package com.shortner.config;

import com.shortner.security.CustomUserDetailsService;
import com.shortner.security.JwtService;
import com.shortner.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // Don't fail the request here on a bad token - just skip authentication and
        // let Spring Security's authorization rules (SecurityConfig) reject the request
        // with a proper 401/403 instead of this filter throwing a raw exception.
        try {
            String username = jwtService.extractUsername(token);

            boolean notYetAuthenticated = SecurityContextHolder.getContext().getAuthentication() == null;

            if (username != null && notYetAuthenticated) {
                UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, principal) && !jwtService.isRefreshToken(token)) {
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Malformed/expired/unknown-user tokens all fall through as "unauthenticated" -
            // no user gets set, downstream authorization handles the rejection.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}