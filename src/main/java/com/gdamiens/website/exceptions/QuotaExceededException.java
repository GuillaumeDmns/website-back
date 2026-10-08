package com.gdamiens.website.exceptions;

import org.springframework.http.HttpStatus;

/**
 * A PRIM call refused by {@code ApiQuota}: the day's quota is used up, or only the guests' share of it. In the latter
 * case the accounts can still call: a cached result must not be replaced by the fallback of a guest.
 */
public class QuotaExceededException extends CustomException {

    private static final long serialVersionUID = 1L;

    public static final String QUOTA_EXHAUSTED = "quota_exhausted";

    public static final String GUEST_QUOTA_EXHAUSTED = "guest_quota_exhausted";

    public QuotaExceededException(String message, String code) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, code);
    }

    public boolean isGuestShare() {
        return GUEST_QUOTA_EXHAUSTED.equals(getCode());
    }
}
