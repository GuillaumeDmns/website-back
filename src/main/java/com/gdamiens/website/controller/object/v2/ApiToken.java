package com.gdamiens.website.controller.object.v2;

import java.time.Instant;

/**
 * A personal API token of the account, without its value (only given at creation)
 *
 * @param lastUsedAt last exchange for an access token, null if never used
 */
public record ApiToken(long id, String name, Instant createdAt, Instant expiresAt, Instant lastUsedAt) {
}
