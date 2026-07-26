package com.shortner.service;

import com.shortner.dto.auth.AuthResponse;
import com.shortner.dto.auth.LoginRequest;
import com.shortner.dto.auth.RegisterRequest;
import com.shortner.entity.User;
import com.shortner.exception.DuplicateAliasException;
import com.shortner.repository.UserRepository;
import com.shortner.security.JwtService;
import com.shortner.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
        UserPrincipal principal = new UserPrincipal(saved);

        return buildAuthResponse(principal);
    }

    public AuthResponse login(LoginRequest request) {
        // Delegates to Spring Security's AuthenticationManager rather than manually
        // comparing password hashes - this ensures CustomUserDetailsService and the
        // configured PasswordEncoder are both exercised the same way in every login path,
        // and BadCredentialsException is thrown/handled consistently (see GlobalExceptionHandler).
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
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