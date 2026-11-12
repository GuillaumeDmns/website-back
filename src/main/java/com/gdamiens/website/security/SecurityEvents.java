package com.gdamiens.website.security;

import com.gdamiens.website.utils.ParisTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * What the administration watches for its security, kept in memory since the start of the service (a restart forgets
 * it): refused credentials and access per kind, today and since the start, and the last admin actions. Anonymous like
 * the other statistics: kinds and times only, never who or from where (the logs say which account ran an action).
 */
@Component
public class SecurityEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityEvents.class);

    /** Admin actions kept */
    static final int MAX_ACTIONS = 50;

    public enum Kind {
        /** A Google ID token refused at sign-in (forged, expired, another client's) */
        GOOGLE_TOKEN_REFUSED,
        /** An unknown, expired or revoked refresh token */
        REFRESH_TOKEN_REFUSED,
        /** A refresh token used again after its rotation: likely stolen, every session of its account was closed */
        REFRESH_TOKEN_REUSED,
        /** An unknown, expired or revoked personal API token */
        API_TOKEN_REFUSED,
        /** An access JWT refused for another reason than its expiry (bad signature, malformed) */
        JWT_REFUSED,
        /** A signed-in caller refused by its role (a user or a guest on the administration) */
        ACCESS_DENIED,
        /** Too many requests on the sign-in endpoints from one address */
        AUTH_RATE_LIMITED
    }

    /** @param today since midnight in Paris */
    public record Count(Kind kind, long today, long sinceStart) {
    }

    /**
     * An admin action (supervision actions, GTFS import) as it ended
     *
     * @param action its id: traffic, elevators, search-index, prim-circuits, sessions-cleanup, gtfs-import
     * @param count  how much it did when it says (stop areas indexed, sessions deleted...), null otherwise
     * @param error  why it failed (kind of error, not its message), null when it didn't
     */
    public record AdminAction(Instant at, String action, Integer count, String error) {
    }

    private final Clock clock;

    private final Map<Kind, Long> sinceStart = new EnumMap<>(Kind.class);

    private final Map<Kind, Long> today = new EnumMap<>(Kind.class);

    private LocalDate day;

    private final Deque<AdminAction> actions = new ArrayDeque<>();

    public SecurityEvents(Clock clock) {
        this.clock = clock;
        this.day = LocalDate.ofInstant(clock.instant(), ParisTime.ZONE);
    }

    public synchronized void record(Kind kind) {
        rollOver();
        sinceStart.merge(kind, 1L, Long::sum);
        today.merge(kind, 1L, Long::sum);
    }

    public synchronized List<Count> counts() {
        rollOver();
        return Arrays.stream(Kind.values())
            .map(kind -> new Count(kind, today.getOrDefault(kind, 0L), sinceStart.getOrDefault(kind, 0L)))
            .toList();
    }

    /** Records an admin action, also in the logs with the account (its login: google:<sub>, no email) */
    public synchronized void adminAction(String login, String action, Integer count, String error) {
        if (error == null) {
            LOGGER.info("Admin action {} by {}: done{}", action, login, count == null ? "" : " (" + count + ")");
        } else {
            LOGGER.warn("Admin action {} by {}: failed ({})", action, login, error);
        }
        actions.addFirst(new AdminAction(clock.instant(), action, count, error));
        while (actions.size() > MAX_ACTIONS) {
            actions.removeLast();
        }
    }

    /** Most recent first */
    public synchronized List<AdminAction> adminActions() {
        return List.copyOf(actions);
    }

    private void rollOver() {
        LocalDate now = LocalDate.ofInstant(clock.instant(), ParisTime.ZONE);
        if (!now.equals(day)) {
            day = now;
            today.clear();
        }
    }
}
