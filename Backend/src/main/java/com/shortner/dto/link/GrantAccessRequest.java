package com.shortner.dto.link;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;

/**
 * Exactly one of username or email must be provided - see isValid(). Supports
 * granting access to an already-registered user by username, or inviting
 * someone who hasn't signed up yet by email (grant sits PENDING until they register).
 */
public record GrantAccessRequest(
    String username,

    @Email(message = "Email must be valid")
    String email
) {
    @AssertTrue(message = "Provide exactly one of username or email")
    public boolean isValid() {
        boolean hasUsername = username != null && !username.isBlank();
        boolean hasEmail = email != null && !email.isBlank();
        return hasUsername ^ hasEmail;
    }
}