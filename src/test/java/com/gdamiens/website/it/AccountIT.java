package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** The signed-in user's account: export and deletion (GDPR, Google Play) */
class AccountIT extends IntegrationTest {

    @Test
    void exportsEverythingKept() {
        SignedIn user = newUser();
        user.api().post("/api/v2/me/favorites", Map.of("kind", "STOP", "stopAreaId", "IDFM:70001"));

        Api.Response export = user.api().get("/api/v2/me/export");

        assertThat(export.status()).isEqualTo(200);
        assertThat(export.header("Content-Disposition")).isEqualTo("attachment; filename=\"account.json\"");
        JsonNode json = export.json();
        assertThat(json.path("exportedAt").asString()).isNotBlank();
        assertThat(json.path("account").path("firstName").asString()).isEqualTo("Ada");
        assertThat(json.path("favorites")).singleElement()
            .satisfies(favorite -> assertThat(favorite.path("stopAreaId").asString()).isEqualTo("IDFM:70001"));
    }

    @Test
    void deletesTheAccountForGood() {
        SignedIn user = newUser();
        user.api().post("/api/v2/me/favorites", Map.of("kind", "LINE", "lineId", "C01742"));

        assertThat(user.api().delete("/api/v2/me").status()).isEqualTo(204);

        // Its access token (valid for an hour) no longer finds it, its sessions are gone
        assertThat(user.api().get("/api/v2/me").status()).isEqualTo(401);
        assertThat(user.api().get("/api/v2/me/favorites").status()).isEqualTo(401);
        assertThat(api.post("/api/token/refresh", Map.of("refreshToken", user.refreshToken())).status()).isEqualTo(401);

        // Signing in again with Google starts a new account
        Api.Response again = api.post("/api/auth/google", Map.of("idToken", GOOGLE.idToken(user.googleSub(), "back@example.com", "Ada")));
        assertThat(api.as(again.json().path("jwt").asString()).get("/api/v2/me/favorites").json()).isEmpty();
    }
}
