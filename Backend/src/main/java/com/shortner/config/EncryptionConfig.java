package com.shortner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Configuration
public class EncryptionConfig {

    private static final int REQUIRED_KEY_BYTES = 32; // 256 bits, for AES-256-GCM

    @Bean
    public Key aesEncryptionKey(@Value("${app.encryption.secret-key}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        // Fail fast at startup rather than throwing a cryptic InvalidKeyException
        // the first time someone creates a link.
        if (keyBytes.length != REQUIRED_KEY_BYTES) {
            throw new IllegalStateException(
                "app.encryption.secret-key must be exactly " + REQUIRED_KEY_BYTES +
                " bytes for AES-256 (got " + keyBytes.length + "). " +
                "Generate one with: openssl rand -hex 16   (produces a 32-character string)"
            );
        }

        return new SecretKeySpec(keyBytes, "AES");
    }
}