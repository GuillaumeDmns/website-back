package com.gdamiens.website.controller.object.v2;

/**
 * A token just created: its value is given this once
 *
 * @param token the value to keep (Bruno environment variable {@code apiToken})
 */
public record NewApiToken(ApiToken apiToken, String token) {
}
