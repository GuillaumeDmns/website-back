package com.gdamiens.website.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Opaque tokens given to clients (refresh tokens, API tokens): 256 random bits, of which only the SHA-256 hash is
 * stored
 */
public final class OpaqueTokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    private OpaqueTokens() {
    }

    /** A new random token, URL-safe, after [prefix] */
    public static String generate(String prefix) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** What is stored: the hex SHA-256 of the token */
    public static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
