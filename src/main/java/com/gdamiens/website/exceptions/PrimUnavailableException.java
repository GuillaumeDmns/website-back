package com.gdamiens.website.exceptions;

import org.springframework.http.HttpStatus;

/**
 * A PRIM API that doesn't answer (timeout, 5xx, its own quota refusal), or that {@code PrimCircuitBreaker} stopped
 * calling for a while. Callers fall back as for any real-time failure (schedule, cache, no vehicles).
 */
public class PrimUnavailableException extends CustomException {

    private static final long serialVersionUID = 1L;

    public static final String PRIM_UNAVAILABLE = "prim_unavailable";

    public PrimUnavailableException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, PRIM_UNAVAILABLE);
    }
}
