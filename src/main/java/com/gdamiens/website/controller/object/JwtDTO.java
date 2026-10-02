package com.gdamiens.website.controller.object;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Tokens returned on sign-in / sign-up / refresh. {@code jwt} is the short-lived access token sent as
 * {@code Bearer}; {@code refreshToken} (absent on the legacy {@code GET /api/refresh}) is used to get a new pair.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtDTO {
    private String jwt;

    private String refreshToken;

    /** Access token lifetime in seconds */
    private Long expiresIn;

    public JwtDTO(String jwt) {
        this.jwt = jwt;
    }

    public JwtDTO(String jwt, String refreshToken, Long expiresIn) {
        this.jwt = jwt;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
