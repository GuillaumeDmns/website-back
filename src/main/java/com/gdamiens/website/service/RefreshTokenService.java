package com.gdamiens.website.service;

import com.gdamiens.website.model.RefreshToken;
import com.gdamiens.website.model.User;
import com.gdamiens.website.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Opaque refresh tokens: 256 random bits given to the client, only their SHA-256 hash is stored.
 * Each use rotates the token; reusing an already rotated token revokes every token of the user (likely theft).
 */
@Service
public class RefreshTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTokenService.class);

    private final SecureRandom secureRandom = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-token.expire-days:60}")
    private long expireDays;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Creates a new refresh token for the user.
     * @return the raw token, to give to the client
     */
    @Transactional
    public String issue(User user) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        refreshTokenRepository.save(new RefreshToken(user, hash(rawToken), OffsetDateTime.now().plusDays(expireDays)));
        return rawToken;
    }

    /**
     * Consumes a refresh token.
     * @return the token's user if the token is valid, empty otherwise
     */
    @Transactional
    public Optional<User> consume(String rawToken) {
        OffsetDateTime now = OffsetDateTime.now();
        Optional<RefreshToken> token = refreshTokenRepository.findByTokenHash(hash(rawToken));

        if (token.isEmpty() || token.get().isExpired(now)) {
            return Optional.empty();
        }

        User user = token.get().getUser();
        if (token.get().isRevoked()) {
            LOGGER.warn("Revoked refresh token reused for user {}, revoking all its tokens", user.getId());
            refreshTokenRepository.revokeAllForUser(user.getId(), now);
            return Optional.empty();
        }

        token.get().setRevokedAt(now);
        return Optional.of(user);
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
            .filter(token -> !token.isRevoked())
            .ifPresent(token -> token.setRevokedAt(OffsetDateTime.now()));
    }

    @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Paris")
    @Transactional
    public void deleteObsoleteTokens() {
        // Revoked tokens are kept a few days to detect their reuse
        int deleted = refreshTokenRepository.deleteObsolete(OffsetDateTime.now().minusDays(7));
        LOGGER.info("Deleted {} obsolete refresh tokens", deleted);
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
