package com.shortner.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Access logs record whether/who accessed a link, but storing raw IPs is an
 * unnecessary privacy liability - a SHA-256 hash is still useful for rate-limiting
 * and abuse detection (same hash = same IP) without persisting the identifiable value.
 */
public final class IpHashUtil {

    private IpHashUtil() {
    }

    public static String hash(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(ipAddress.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed available on every standard JVM - this branch is unreachable in practice.
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}