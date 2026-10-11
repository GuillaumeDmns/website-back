package com.gdamiens.website.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void recognizesGuests() {
        assertThat(Role.isGuest(new TestingAuthenticationToken("guest:1", null, "ROLE_GUEST"))).isTrue();
        assertThat(Role.isGuest(new TestingAuthenticationToken("ada", null, "ROLE_USER"))).isFalse();
        assertThat(Role.isGuest(new TestingAuthenticationToken("admin", null, "ROLE_ADMIN"))).isFalse();
        // Background tasks have no authentication: they count as accounts
        assertThat(Role.isGuest(null)).isFalse();
    }
}
