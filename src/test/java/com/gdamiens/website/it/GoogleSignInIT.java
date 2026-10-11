package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Sign-in with Google: accounts, admin role, refused tokens */
class GoogleSignInIT extends IntegrationTest {

    @Test
    void theFirstSignInCreatesTheAccount() {
        String email = "ada-" + UUID.randomUUID() + "@example.com";
        SignedIn user = signInWithGoogle(email, "Ada");

        JsonNode account = user.api().get("/api/v2/me").json();

        assertThat(account.path("email").asString()).isEqualTo(email);
        assertThat(account.path("firstName").asString()).isEqualTo("Ada");
        assertThat(account.path("role").asString()).isEqualTo("ROLE_USER");
        assertThat(account.path("google").asBoolean()).isTrue();
        assertThat(account.path("createdAt").asString()).isNotBlank();
    }

    @Test
    void theSameGoogleUserFindsItsAccountWithItsNewEmailAndName() {
        SignedIn first = newUser();
        first.api().post("/api/v2/me/favorites", Map.of("kind", "LINE", "lineId", "C01371"));

        String newEmail = "renamed-" + UUID.randomUUID() + "@example.com";
        Api.Response again = api.post("/api/auth/google", Map.of("idToken", GOOGLE.idToken(first.googleSub(), newEmail, "Augusta")));
        Api second = api.as(again.json().path("jwt").asString());

        JsonNode account = second.get("/api/v2/me").json();
        assertThat(account.path("email").asString()).isEqualTo(newEmail);
        assertThat(account.path("firstName").asString()).isEqualTo("Augusta");
        assertThat(second.get("/api/v2/me/favorites").json()).hasSize(1);
    }

    @Test
    void anAdminEmailGetsTheAdminRole() {
        SignedIn admin = admin();

        assertThat(admin.api().get("/api/v2/me").json().path("role").asString()).isEqualTo("ROLE_ADMIN");
        assertThat(admin.api().get("/api/admin/status").status()).isEqualTo(200);
        assertThat(newUser().api().get("/api/admin/status").status()).isEqualTo(403);
    }

    @Test
    void anEmailOfAPasswordAccountIsNotTaken() {
        String username = "pw" + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@example.com";
        assertThat(api.post("/api/signup", Map.of("username", username, "email", email, "password", "correct horse")).status()).isEqualTo(201);

        SignedIn google = signInWithGoogle(email, "Ada");

        // Two accounts, never merged: the Google one keeps no email
        assertThat(google.api().get("/api/v2/me").json().path("email").isNull()).isTrue();
    }

    @Test
    void refusesInvalidTokens() {
        assertThat(api.post("/api/auth/google", Map.of("idToken", GOOGLE.forgedIdToken("1", "x@example.com"))).status()).isEqualTo(401);
        assertThat(api.post("/api/auth/google", Map.of("idToken", GOOGLE.idToken("1", "x@example.com", "X",
            claims -> claims.audience("another-app")))).status()).isEqualTo(401);
        assertThat(api.post("/api/auth/google", Map.of("idToken", "")).status()).isEqualTo(400);
        assertThat(api.post("/api/auth/google", "{not json").status()).isEqualTo(400);
    }

    @Test
    void googleTokensComeWithARefreshToken() {
        SignedIn user = newUser();

        Api.Response refreshed = api.post("/api/token/refresh", Map.of("refreshToken", user.refreshToken()));

        assertThat(refreshed.status()).isEqualTo(200);
        assertThat(api.as(refreshed.json().path("jwt").asString()).get("/api/v2/me").json().path("google").asBoolean()).isTrue();
    }
}
