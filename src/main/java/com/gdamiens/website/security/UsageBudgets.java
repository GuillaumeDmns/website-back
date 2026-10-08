package com.gdamiens.website.security;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.CustomException;
import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Usage allowed per guest device or account over hours and days (the per-minute limit is {@link RateLimitFilter}'s),
 * for what costs a PRIM call each time: the journey searches. In memory, like {@link RateLimiter}.
 */
@Component
public class UsageBudgets {

    /** Problem detail code: no more journey searches for this account for now */
    public static final String JOURNEY_LIMIT = "journey_limit";

    /** Problem detail code: no more journey searches for this guest device today, signing in gives more */
    public static final String GUEST_JOURNEY_LIMIT = "guest_journey_limit";

    private static final Duration DAY = Duration.ofDays(1);

    private final RateLimiter rateLimiter;

    private final ApplicationProperties.Budget budget;

    public UsageBudgets(RateLimiter rateLimiter, ApplicationProperties applicationProperties) {
        this.rateLimiter = rateLimiter;
        this.budget = applicationProperties.getBudget();
    }

    /**
     * Counts a journey search of the caller
     *
     * @throws CustomException 429 ({@link #JOURNEY_LIMIT} or {@link #GUEST_JOURNEY_LIMIT}) when its budget is used up
     */
    public void consumeJourney(Authentication authentication) {
        boolean guest = Role.isGuest(authentication);
        boolean consumed = guest
            ? rateLimiter.tryConsume("journeys:" + authentication.getName(), DAY, () -> Bucket.builder()
                .addLimit(limit -> limit.capacity(budget.getGuestJourneysPerDay()).refillGreedy(budget.getGuestJourneysPerDay(), DAY))
                .build()).isConsumed()
            : rateLimiter.tryConsume("journeys:" + authentication.getName(), DAY, () -> Bucket.builder()
                .addLimit(limit -> limit.capacity(budget.getUserJourneysPerDay()).refillGreedy(budget.getUserJourneysPerDay(), DAY))
                .addLimit(limit -> limit.capacity(budget.getUserJourneysPerHour()).refillGreedy(budget.getUserJourneysPerHour(), Duration.ofHours(1)))
                .build()).isConsumed();

        if (!consumed) {
            throw new CustomException(guest ? "Journey searches of this device used up for today, sign in for more"
                : "Too many journey searches, retry later", HttpStatus.TOO_MANY_REQUESTS, guest ? GUEST_JOURNEY_LIMIT : JOURNEY_LIMIT);
        }
    }

    /**
     * Counts a guest token given to an IP address
     *
     * @throws CustomException 429 when the address got too many this hour
     */
    public void consumeGuestToken(String ip) {
        int perHour = budget.getGuestTokensPerIpPerHour();
        boolean consumed = rateLimiter.tryConsume("guest-tokens:" + ip, Duration.ofHours(1), () -> Bucket.builder()
            .addLimit(limit -> limit.capacity(perHour).refillGreedy(perHour, Duration.ofHours(1)))
            .build()).isConsumed();

        if (!consumed) {
            throw new CustomException("Too many guest tokens from this address, retry later", HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
