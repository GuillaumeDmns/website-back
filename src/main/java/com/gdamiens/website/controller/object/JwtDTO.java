package com.gdamiens.website.controller.object;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Tokens returned on sign-in / sign-up / refresh. {@code jwt} is the short-lived access token sent as
 * {@code Bearer}; {@code refreshToken} (absent for a guest token) is used to get a new pair.
 *
 * @param expiresIn access token lifetime in seconds
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JwtDTO(String jwt, String refreshToken, Long expiresIn) {
}
