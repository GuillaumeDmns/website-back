package com.gdamiens.website.service;

import com.gdamiens.website.exceptions.PrimUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Stops calling a PRIM API that keeps failing, so that requests don't pile up waiting for its timeouts: after
 * {@link #FAILURES} failures in a row (no answer, 5xx), calls to it fail at once for {@link #OPEN_FOR}
 * ({@link #QUOTA_OPEN_FOR} after a 429, PRIM's own quota refusal), then a single call is let through to try again.
 * Refused calls don't count against the quotas. Per API, in memory.
 */
@Component
public class PrimCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(PrimCircuitBreaker.class);

    static final int FAILURES = 5;

    static final Duration OPEN_FOR = Duration.ofSeconds(30);

    static final Duration QUOTA_OPEN_FOR = Duration.ofMinutes(5);

    public enum State {
        /** Calls go through */
        CLOSED,
        /** Calls are refused until {@code openUntil} */
        OPEN,
        /** One call is trying again */
        TRIAL
    }

    /** State of an API, for the admin status */
    public record Status(ApiQuota.Api api, State state, int failures, Instant openUntil, Instant lastFailureAt, String lastError) {
    }

    private static final class Circuit {
        int failures;
        Instant openUntil;
        boolean trial;
        Instant lastFailureAt;
        String lastError;
    }

    private final Map<ApiQuota.Api, Circuit> circuits = new EnumMap<>(ApiQuota.Api.class);

    public PrimCircuitBreaker() {
        for (ApiQuota.Api api : ApiQuota.Api.values()) {
            circuits.put(api, new Circuit());
        }
    }

    /**
     * Before a call to [api]: report its outcome with {@link #success}, {@link #failure} or {@link #abandon}
     *
     * @throws PrimUnavailableException while the circuit is open, or while another call is trying again
     */
    public synchronized void beforeCall(ApiQuota.Api api) {
        Circuit circuit = circuits.get(api);
        if (circuit.openUntil == null) {
            return;
        }
        if (Instant.now().isBefore(circuit.openUntil) || circuit.trial) {
            throw new PrimUnavailableException(api + " unavailable since " + circuit.lastFailureAt + ": " + circuit.lastError);
        }
        circuit.trial = true;
    }

    /** The call got an answer (an error of the request itself, e.g. a 4xx, counts as an answer) */
    public synchronized void success(ApiQuota.Api api) {
        Circuit circuit = circuits.get(api);
        if (circuit.openUntil != null) {
            log.info("{} answers again", api);
        }
        circuit.failures = 0;
        circuit.openUntil = null;
        circuit.trial = false;
    }

    /**
     * The call got no answer, a 5xx, or a 429 ([quota]: PRIM refuses until its quota is reset, so it is left alone
     * longer)
     */
    public synchronized void failure(ApiQuota.Api api, String error, boolean quota) {
        Circuit circuit = circuits.get(api);
        circuit.failures++;
        circuit.lastFailureAt = Instant.now();
        circuit.lastError = error;
        if (circuit.trial || quota || circuit.failures >= FAILURES) {
            Duration openFor = quota ? QUOTA_OPEN_FOR : OPEN_FOR;
            circuit.openUntil = circuit.lastFailureAt.plus(openFor);
            circuit.trial = false;
            log.warn("{} failed {} time(s) in a row, not called for {} s: {}", api, circuit.failures, openFor.toSeconds(), error);
        }
    }

    /** The call was not made after all (e.g. refused by {@link ApiQuota}) */
    public synchronized void abandon(ApiQuota.Api api) {
        circuits.get(api).trial = false;
    }

    public synchronized List<Status> status() {
        Instant now = Instant.now();
        List<Status> status = new ArrayList<>();
        circuits.forEach((api, circuit) -> {
            State state = circuit.openUntil == null ? State.CLOSED
                : circuit.trial ? State.TRIAL
                : now.isBefore(circuit.openUntil) ? State.OPEN
                : State.TRIAL;
            status.add(new Status(api, state, circuit.failures, circuit.openUntil, circuit.lastFailureAt, circuit.lastError));
        });
        return status;
    }
}
