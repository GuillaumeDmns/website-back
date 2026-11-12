package com.gdamiens.website.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebSecurityConfigTest {

    private static WebSecurityConfig withSecret(String secret) {
        WebSecurityConfig config = new WebSecurityConfig();
        ReflectionTestUtils.setField(config, "jwtSecretKey", secret);
        return config;
    }

    @Test
    void refusesToStartOnAShortSecret() {
        // Whoever finds the secret signs any admin's token: one that can be tried out never signs any
        assertThatThrownBy(() -> withSecret("short-secret").jwtDecoder()).isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("at least 32");
        assertThatThrownBy(() -> withSecret(null).jwtEncoder()).isInstanceOf(IllegalStateException.class);
        assertThatNoException().isThrownBy(() -> withSecret("0123456789abcdef0123456789abcdef").jwtDecoder());
    }

    @Test
    void anExpiredTokenIsNoAttack() {
        JwtValidationException expired = new JwtValidationException("expired",
            List.of(new OAuth2Error("invalid_token", "Jwt expired at 2026-11-12T10:00:00Z", null)));
        JwtValidationException wrongIssuer = new JwtValidationException("invalid",
            List.of(new OAuth2Error("invalid_token", "The iss claim is not valid", null)));

        assertThat(WebSecurityConfig.isForgedOrMalformed(new InvalidBearerTokenException("expired", expired))).isFalse();
        assertThat(WebSecurityConfig.isForgedOrMalformed(new InvalidBearerTokenException("invalid", wrongIssuer))).isTrue();
        // Bad signature, garbage
        assertThat(WebSecurityConfig.isForgedOrMalformed(new InvalidBearerTokenException("bad", new BadJwtException("signature")))).isTrue();
        // No token at all: not a refused one
        assertThat(WebSecurityConfig.isForgedOrMalformed(new InsufficientAuthenticationException("none"))).isFalse();
    }
}
