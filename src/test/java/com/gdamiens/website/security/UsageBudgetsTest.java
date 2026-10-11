package com.gdamiens.website.security;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsageBudgetsTest {

    private UsageBudgets budgets;

    @BeforeEach
    void setUp() {
        ApplicationProperties properties = new ApplicationProperties();
        properties.getBudget().setGuestJourneysPerDay(3);
        properties.getBudget().setUserJourneysPerDay(10);
        properties.getBudget().setUserJourneysPerHour(5);
        properties.getBudget().setGuestTokensPerIpPerHour(2);
        budgets = new UsageBudgets(new RateLimiter(), properties);
    }

    @Test
    void guestsGetAFewJourneysADay() {
        Authentication guest = guest("guest:1");
        for (int i = 0; i < 3; i++) {
            budgets.consumeJourney(guest);
        }

        assertThatThrownBy(() -> budgets.consumeJourney(guest))
            .isInstanceOfSatisfying(CustomException.class, e -> {
                assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                assertThat(e.getCode()).isEqualTo(UsageBudgets.GUEST_JOURNEY_LIMIT);
            });
        // Another device has its own budget
        assertThatNoException().isThrownBy(() -> budgets.consumeJourney(guest("guest:2")));
    }

    @Test
    void accountsAreLimitedPerHour() {
        Authentication user = new TestingAuthenticationToken("ada", null, Role.ROLE_USER.name());
        for (int i = 0; i < 5; i++) {
            budgets.consumeJourney(user);
        }

        assertThatThrownBy(() -> budgets.consumeJourney(user))
            .isInstanceOfSatisfying(CustomException.class, e -> assertThat(e.getCode()).isEqualTo(UsageBudgets.JOURNEY_LIMIT));
    }

    @Test
    void guestTokensAreLimitedPerAddress() {
        budgets.consumeGuestToken("203.0.113.1");
        budgets.consumeGuestToken("203.0.113.1");

        assertThatThrownBy(() -> budgets.consumeGuestToken("203.0.113.1"))
            .isInstanceOfSatisfying(CustomException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
        assertThatNoException().isThrownBy(() -> budgets.consumeGuestToken("203.0.113.2"));
    }

    private static Authentication guest(String id) {
        return new TestingAuthenticationToken(id, null, Role.ROLE_GUEST.name());
    }
}
