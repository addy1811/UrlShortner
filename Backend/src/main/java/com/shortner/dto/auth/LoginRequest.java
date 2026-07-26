package com.shortner.dto.auth;

import jakarta.validation.constraints.NotBlank;
 
public record LoginRequest(
 
    // Accepts either username or email - resolved in AuthService.
    @NotBlank(message = "Username or email is required")
    String usernameOrEmail,
 
    @NotBlank(message = "Password is required")
    String password
 
) {}
 