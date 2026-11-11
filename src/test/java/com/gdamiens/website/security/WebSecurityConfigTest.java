package com.gdamiens.website.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
}
