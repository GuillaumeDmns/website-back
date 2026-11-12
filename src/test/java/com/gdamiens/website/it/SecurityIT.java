package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** What an attacker could try, and what the administration's security page shows of it */
class SecurityIT extends IntegrationTest {

    private static long today(JsonNode security, String kind) {
        return security.path("events").values().stream()
            .filter(event -> kind.equals(event.path("kind").asString()))
            .findFirst().orElseThrow().path("today").asLong();
    }

    private Api apiTokenSession(Api admin) {
        String token = admin.post("/api/v2/me/api-tokens", Map.of("name", "Script")).json().path("token").asString();
        return api.as(api.post("/api/auth/api-token", Map.of("token", token)).json().path("jwt").asString());
    }

    @Test
    void aLeakedApiTokenCannotMakeOthersNorDeleteTheAccount() {
        Api script = apiTokenSession(admin().api());

        Api.Response created = script.post("/api/v2/me/api-tokens", Map.of("name", "Another"));
        assertThat(created.status()).isEqualTo(403);
        assertThat(created.json().path("code").asString()).isEqualTo("api_token_session");
        assertThat(script.delete("/api/v2/me").json().path("code").asString()).isEqualTo("api_token_session");

        // What it is for still works
        assertThat(script.get("/api/admin/status").status()).isEqualTo(200);
        assertThat(script.get("/api/v2/me/api-tokens").status()).isEqualTo(200);
    }

    @Test
    void anAccountSignsOutEverywhere() {
        String email = "everywhere-" + UUID.randomUUID() + "@example.com";
        SignedIn user = signInWithGoogle(email, "Ada");
        Api.Response second = api.post("/api/token/refresh", Map.of("refreshToken", user.refreshToken()));
        String refreshToken = second.json().path("refreshToken").asString();
        // Signed in on another device too
        String otherDevice = api.post("/api/auth/google", Map.of("idToken", GOOGLE.idToken(user.googleSub(), email, "Ada")))
            .json().path("refreshToken").asString();

        assertThat(user.api().get("/api/v2/me/sessions").json().path("active").asInt()).isEqualTo(2);

        assertThat(user.api().delete("/api/v2/me/sessions").json().path("closed").asInt()).isEqualTo(2);

        assertThat(api.post("/api/token/refresh", Map.of("refreshToken", refreshToken)).status()).isEqualTo(401);
        assertThat(api.post("/api/token/refresh", Map.of("refreshToken", otherDevice)).status()).isEqualTo(401);
        assertThat(user.api().get("/api/v2/me/sessions").json().path("active").asInt()).isZero();
        assertThat(guest().get("/api/v2/me/sessions").status()).isEqualTo(403);
    }

    @Test
    void theAdministrationSeesRefusedCredentialsAndAccess() {
        Api admin = admin().api();
        JsonNode before = admin.get("/api/admin/stats/security").json();

        newUser().api().get("/api/admin/stats/security");
        api.post("/api/token/refresh", Map.of("refreshToken", "not-a-token"));
        api.post("/api/auth/api-token", Map.of("token", "gdpat_unknown"));
        api.as("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4In0.forged").get("/api/v2/traffic");

        JsonNode after = admin.get("/api/admin/stats/security").json();
        assertThat(today(after, "ACCESS_DENIED")).isGreaterThan(today(before, "ACCESS_DENIED"));
        assertThat(today(after, "REFRESH_TOKEN_REFUSED")).isGreaterThan(today(before, "REFRESH_TOKEN_REFUSED"));
        assertThat(today(after, "API_TOKEN_REFUSED")).isGreaterThan(today(before, "API_TOKEN_REFUSED"));
        assertThat(today(after, "JWT_REFUSED")).isGreaterThan(today(before, "JWT_REFUSED"));
        assertThat(after.path("access").path("admins").asInt()).isPositive();
        assertThat(after.path("adminEmails").asInt()).isEqualTo(1);
        assertThat(after.path("since").asString()).isNotBlank();
    }

    @Test
    void aStolenRefreshTokenUsedAgainIsSeen() {
        SignedIn user = newUser();
        Api admin = admin().api();
        long before = today(admin.get("/api/admin/stats/security").json(), "REFRESH_TOKEN_REUSED");

        api.post("/api/token/refresh", Map.of("refreshToken", user.refreshToken()));
        api.post("/api/token/refresh", Map.of("refreshToken", user.refreshToken()));

        assertThat(today(admin.get("/api/admin/stats/security").json(), "REFRESH_TOKEN_REUSED")).isEqualTo(before + 1);
    }

    @Test
    void adminActionsAreJournaledOnTheServer() {
        Api admin = admin().api();

        admin.post("/api/admin/actions/sessions-cleanup");

        JsonNode last = admin.get("/api/admin/stats/security").json().path("actions").get(0);
        assertThat(last.path("action").asString()).isEqualTo("sessions-cleanup");
        assertThat(last.path("error").isNull()).isTrue();
        assertThat(last.path("count").isNumber()).isTrue();
    }

    @Test
    void onlyAdminsSeeIt() {
        assertThat(newUser().api().get("/api/admin/stats/security").status()).isEqualTo(403);
        assertThat(guest().get("/api/admin/stats/security").status()).isEqualTo(403);
        assertThat(api.get("/api/admin/stats/security").status()).isEqualTo(401);
    }
}
