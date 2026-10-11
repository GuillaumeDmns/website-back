package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Personal API tokens of the admins: what Bruno and scripts use instead of a password */
class ApiTokenIT extends IntegrationTest {

    @Test
    void anAdminCreatesATokenAndBrunoSignsInWithIt() {
        Api admin = admin().api();

        Api.Response created = admin.post("/api/v2/me/api-tokens", Map.of("name", "Bruno"));

        assertThat(created.status()).isEqualTo(201);
        String token = created.json().path("token").asString();
        assertThat(token).startsWith("gdpat_");
        assertThat(created.json().path("apiToken").path("name").asString()).isEqualTo("Bruno");

        Api.Response exchanged = api.post("/api/auth/api-token", Map.of("token", token));
        assertThat(exchanged.status()).isEqualTo(200);
        assertThat(exchanged.json().has("refreshToken")).isFalse();
        Api bruno = api.as(exchanged.json().path("jwt").asString());
        assertThat(bruno.get("/api/v2/me").json().path("role").asString()).isEqualTo("ROLE_ADMIN");
        assertThat(bruno.get("/api/admin/status").status()).isEqualTo(200);

        // Listed without its value, with its last use
        JsonNode listed = admin.get("/api/v2/me/api-tokens").json();
        assertThat(listed).singleElement().satisfies(item -> {
            assertThat(item.has("token")).isFalse();
            assertThat(item.path("lastUsedAt").isNull()).isFalse();
            assertThat(item.path("expiresAt").asString()).isNotBlank();
        });
    }

    @Test
    void aRevokedTokenNoLongerSignsIn() {
        Api admin = admin().api();
        JsonNode created = admin.post("/api/v2/me/api-tokens", Map.of("name", "Laptop", "validDays", 30)).json();
        long id = created.path("apiToken").path("id").asLong();

        assertThat(admin.delete("/api/v2/me/api-tokens/" + id).status()).isEqualTo(204);

        assertThat(api.post("/api/auth/api-token", Map.of("token", created.path("token").asString())).status()).isEqualTo(401);
        assertThat(admin.delete("/api/v2/me/api-tokens/" + id).status()).isEqualTo(404);
    }

    @Test
    void onlyAdminsHaveTokens() {
        assertThat(newUser().api().post("/api/v2/me/api-tokens", Map.of("name", "Script")).status()).isEqualTo(403);
        assertThat(newUser().api().get("/api/v2/me/api-tokens").status()).isEqualTo(403);
        assertThat(guest().get("/api/v2/me/api-tokens").status()).isEqualTo(403);
    }

    @Test
    void checksTheName() {
        Api admin = admin().api();

        assertThat(admin.post("/api/v2/me/api-tokens", Map.of("name", " ")).status()).isEqualTo(400);
        assertThat(admin.post("/api/v2/me/api-tokens", Map.of("name", "x".repeat(101))).status()).isEqualTo(400);
    }

    @Test
    void refusesUnknownTokens() {
        assertThat(api.post("/api/auth/api-token", Map.of("token", "gdpat_unknown")).status()).isEqualTo(401);
        assertThat(api.post("/api/auth/api-token", Map.of("token", "not-a-token")).status()).isEqualTo(401);
        assertThat(api.post("/api/auth/api-token", Map.of()).status()).isEqualTo(401);
    }
}
