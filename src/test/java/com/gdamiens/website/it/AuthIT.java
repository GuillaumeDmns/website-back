package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Tokens: guests, refresh token rotation, sign-out, no more passwords */
class AuthIT extends IntegrationTest {

    @Test
    void aGuestTokenOpensThePublicApiButNotAnAccount() {
        Api.Response response = api.post("/api/auth/guest");

        assertThat(response.status()).isEqualTo(200);
        JsonNode tokens = response.json();
        assertThat(tokens.has("refreshToken")).isFalse();
        assertThat(tokens.path("expiresIn").asLong()).isEqualTo(30L * 24 * 3600);

        Api guest = api.as(tokens.path("jwt").asString());
        assertThat(guest.get("/api/v2/lines/C01371").status()).isEqualTo(200);
        assertThat(guest.get("/api/v2/me").status()).isEqualTo(403);
        assertThat(guest.get("/api/v2/me/favorites").status()).isEqualTo(403);
    }

    @Test
    void theApiNeedsAToken() {
        assertThat(api.get("/api/v2/lines/C01371").status()).isEqualTo(401);
        assertThat(api.as("not-a-jwt").get("/api/v2/lines/C01371").status()).isEqualTo(401);
    }

    @Test
    void refreshTokensRotateAndAReuseRevokesThemAll() {
        SignedIn user = newUser();

        Api.Response first = api.post("/api/token/refresh", Map.of("refreshToken", user.refreshToken()));
        assertThat(first.status()).isEqualTo(200);
        String next = first.json().path("refreshToken").asString();
        assertThat(next).isNotEqualTo(user.refreshToken());
        assertThat(api.as(first.json().path("jwt").asString()).get("/api/v2/me").status()).isEqualTo(200);

        // The rotated token comes back: likely stolen, every token of the user is revoked
        assertThat(api.post("/api/token/refresh", Map.of("refreshToken", user.refreshToken())).status()).isEqualTo(401);
        assertThat(api.post("/api/token/refresh", Map.of("refreshToken", next)).status()).isEqualTo(401);
    }

    @Test
    void signingOutRevokesTheRefreshToken() {
        SignedIn user = newUser();

        assertThat(api.post("/api/logout", Map.of("refreshToken", user.refreshToken())).status()).isEqualTo(204);

        assertThat(api.post("/api/token/refresh", Map.of("refreshToken", user.refreshToken())).status()).isEqualTo(401);
        // Signing out twice is fine
        assertThat(api.post("/api/logout", Map.of("refreshToken", user.refreshToken())).status()).isEqualTo(204);
    }

    @Test
    void refreshAndLogoutNeedAToken() {
        assertThat(api.post("/api/token/refresh", Map.of()).status()).isEqualTo(400);
        assertThat(api.post("/api/logout", Map.of()).status()).isEqualTo(400);
        assertThat(api.post("/api/token/refresh", Map.of("refreshToken", "unknown")).status()).isEqualTo(401);
    }

    @Test
    void passwordSignInIsGone() {
        assertThat(api.post("/api/signin", Map.of("username", "admin", "password", "secret")).status()).isEqualTo(401);
        assertThat(api.post("/api/signup", Map.of("username", "ada", "email", "ada@example.com", "password", "secret")).status())
            .isEqualTo(401);
    }

    @Test
    void authEndpointsAreRateLimitedPerAddress() {
        Api client = api.from("198.19.0.1");
        for (int i = 0; i < 10; i++) {
            assertThat(client.post("/api/auth/api-token", Map.of("token", "gdpat_wrong")).status()).isEqualTo(401);
        }

        Api.Response limited = client.post("/api/auth/api-token", Map.of("token", "gdpat_wrong"));

        assertThat(limited.status()).isEqualTo(429);
        assertThat(limited.header("Retry-After")).isNotNull();
        assertThat(limited.json().path("status").asInt()).isEqualTo(429);
        // Another address is not limited
        assertThat(api.post("/api/auth/api-token", Map.of("token", "gdpat_wrong")).status()).isEqualTo(401);
    }
}
