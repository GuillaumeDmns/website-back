package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.ApiToken;
import com.gdamiens.website.controller.object.v2.NewApiToken;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.model.User;
import com.gdamiens.website.repository.ApiTokenRepository;
import com.gdamiens.website.utils.OpaqueTokens;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Personal API tokens: what Bruno and scripts use since sign-in goes through Google. Long-lived, opaque
 * ({@link OpaqueTokens}, only the hash is stored), exchanged for a short access JWT ({@code POST /api/auth/api-token}).
 * Admins only (see {@code ApiTokenController}): a public token factory would let scripts drain the quotas.
 */
@Service
public class ApiTokenService {

    /** Tokens are recognisable (secret scanners, logs) */
    static final String PREFIX = "gdpat_";

    static final int MAX_TOKENS = 10;

    static final int MAX_DAYS = 365;

    private final ApiTokenRepository apiTokenRepository;

    private final Clock clock;

    public ApiTokenService(ApiTokenRepository apiTokenRepository, Clock clock) {
        this.apiTokenRepository = apiTokenRepository;
        this.clock = clock;
    }

    /**
     * @throws CustomException 400 without name, or with too many tokens already
     */
    @Transactional
    public NewApiToken create(User user, String name, Integer validDays) {
        String label = StringUtils.trimToNull(name);
        if (label == null || label.length() > 100) {
            throw new CustomException("A name of at most 100 characters is required", HttpStatus.BAD_REQUEST);
        }
        if (apiTokenRepository.countByUser(user.getId()) >= MAX_TOKENS) {
            throw new CustomException("At most " + MAX_TOKENS + " API tokens, delete one first", HttpStatus.BAD_REQUEST);
        }
        int days = validDays == null ? MAX_DAYS : Math.clamp(validDays, 1, MAX_DAYS);
        String token = OpaqueTokens.generate(PREFIX);
        ApiToken created = apiTokenRepository.create(user.getId(), label, OpaqueTokens.hash(token),
            clock.instant().plus(Duration.ofDays(days)));
        return new NewApiToken(created, token);
    }

    public List<ApiToken> list(User user) {
        return apiTokenRepository.findByUser(user.getId());
    }

    /**
     * @throws CustomException 404 if the user has no such token
     */
    public void delete(User user, long id) {
        if (!apiTokenRepository.delete(user.getId(), id)) {
            throw new CustomException("API token not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * @return the id of the token's user when it is valid, empty otherwise
     */
    @Transactional
    public Optional<Integer> use(String token) {
        if (token == null || !token.startsWith(PREFIX)) {
            return Optional.empty();
        }
        return apiTokenRepository.use(OpaqueTokens.hash(token), clock.instant());
    }
}
