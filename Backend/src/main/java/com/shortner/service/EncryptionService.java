package com.shortner.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;   // 96 bits - the NIST-recommended IV size for GCM
    private static final int GCM_TAG_LENGTH_BITS = 128;  // authentication tag size

    private final Key aesEncryptionKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public record EncryptedPayload(byte[] ciphertext, byte[] iv) {}

    public EncryptedPayload encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv); // fresh, unpredictable IV per encryption - never reuse an IV with the same key

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesEncryptionKey, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return new EncryptedPayload(ciphertext, iv);

        } catch (GeneralSecurityException e) {
            // Wrapped as unchecked - a crypto failure here means misconfiguration
            // (bad key length, unsupported algorithm), not a recoverable request-level error.
            throw new IllegalStateException("Failed to encrypt destination URL", e);
        }
    }

    public String decrypt(byte[] ciphertext, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesEncryptionKey, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (GeneralSecurityException e) {
            // Covers tampered ciphertext (GCM auth tag mismatch) as well as genuine errors -
            // both should be treated as "this data can't be trusted", not silently ignored.
            throw new IllegalStateException("Failed to decrypt destination URL - data may be corrupted or tampered", e);
        }
    }
}