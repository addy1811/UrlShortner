package com.shortner.service;

import com.shortner.repository.ShortLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
 
import java.security.SecureRandom;
 
@Service
@RequiredArgsConstructor
public class ShortCodeGeneratorService {
 
    // Alphanumeric, mixed-case - avoids ambiguous-looking sets like "0/O" or "1/l/I"
    // by simply including everything (Base62); collisions across 8 chars are astronomically rare anyway.
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MAX_GENERATION_ATTEMPTS = 10;
 
    private final ShortLinkRepository shortLinkRepository;
    private final SecureRandom secureRandom = new SecureRandom();
 
    @Value("${app.short-link.code-length:8}")
    private int codeLength;
 
    /**
     * Generates a random short code and confirms it's not already in use.
     * Using SecureRandom (not a sequential ID or Hashids-style reversible encoding)
     * is what makes codes non-enumerable - you can't guess link B by incrementing link A.
     */
    public String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = generateRandomCode();
            if (!shortLinkRepository.existsByShortCode(candidate)) {
                return candidate;
            }
        }
        // Practically unreachable at 8 chars (62^8 ≈ 218 trillion combinations), but fail
        // loudly rather than silently returning a colliding code if it ever does happen.
        throw new IllegalStateException(
            "Failed to generate a unique short code after " + MAX_GENERATION_ATTEMPTS + " attempts"
        );
    }
 
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}