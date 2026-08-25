package com.shortner.service;

import com.shortner.dto.auth.AuthResponse;
import com.shortner.dto.auth.LoginRequest;
import com.shortner.dto.auth.RefreshTokenRequest;
import com.shortner.dto.auth.RegisterRequest;
import com.shortner.entity.GrantStatus;
import com.shortner.entity.LinkAccessGrant;
import com.shortner.entity.User;
import com.shortner.exception.DuplicateAliasException;
import com.shortner.repository.LinkAccessGrantRepository;
import com.shortner.repository.UserRepository;
import com.shortner.security.CustomUserDetailsService;
import com.shortner.security.JwtService;
import com.shortner.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final LinkAccessGrantRepository linkAccessGrantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateAliasException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateAliasException("Email is already registered");
        }

        User user = User.builder()
            .username(request.username())
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .build();

        User saved = userRepository.save(user);
        List<LinkAccessGrant> pendingGrants =
            linkAccessGrantRepository.findByInvitedEmailAndStatus(saved.getEmail(), GrantStatus.PENDING);

        if (!pendingGrants.isEmpty()) {
            pendingGrants.forEach(grant -> {
                grant.setGrantee(saved);
                grant.setStatus(GrantStatus.ACTIVE);
            });
            linkAccessGrantRepository.saveAll(pendingGrants);
        }

        UserPrincipal principal = new UserPrincipal(saved);
        return buildAuthResponse(principal);
    }

    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildAuthResponse(principal);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (jwtService.isTokenExpired(token) || !jwtService.isRefreshToken(token)) {
            throw new IllegalStateException("Invalid or expired refresh token");
        }

        String username = jwtService.extractUsername(token);
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(token, principal)) {
            throw new IllegalStateException("Invalid or expired refresh token");
        }

        return buildAuthResponse(principal);
    }

    private AuthResponse buildAuthResponse(UserPrincipal principal) {
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        return new AuthResponse(
            accessToken,
            refreshToken,
            jwtService.getAccessTokenExpiry(),
            principal.getId(),
            principal.getUsername()
        );
    }
}