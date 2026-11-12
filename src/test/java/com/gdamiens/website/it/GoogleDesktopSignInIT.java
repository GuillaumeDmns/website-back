package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.FakeServer;
import com.gdamiens.website.support.FakeServer.Response;
import com.gdamiens.website.support.GoogleTokens;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sign-in with Google of the desktop apps: the app starts it, the browser goes through Google's page (here: comes back
 * from it with a code) and brings a one-time code to the app's port, the app exchanges it for its tokens
 */
class GoogleDesktopSignInIT extends IntegrationTest {

    private static final int APP_PORT = 51234;

    @Test
    void aDesktopAppSignsInThroughTheBrowser() {
        String email = "desktop-" + UUID.randomUUID() + "@example.com";
        JsonNode started = start();
        googleSignsIn(started, UUID.randomUUID().toString(), email, "Ada");

        MultiValueMap<String, String> page = query(started.path("authorizationUrl").asString());
        assertThat(started.path("authorizationUrl").asString()).startsWith(EXTERNAL.url() + "/google/auth?");
        assertThat(page.getFirst("client_id")).isEqualTo(GoogleTokens.CLIENT_ID);
        assertThat(page.getFirst("state")).isEqualTo(started.path("state").asString());

        Api.Response back = browserComesBack(started, "google-code");

        assertThat(back.status()).isEqualTo(302);
        String app = back.header("Location");
        assertThat(app).startsWith("http://127.0.0.1:" + APP_PORT + "/?code=");
        assertThat(query(app).getFirst("state")).isEqualTo(started.path("state").asString());
        FakeServer.Request exchange = EXTERNAL.requests("/google/token").getFirst();
        assertThat(exchange.field("code")).isEqualTo("google-code");
        assertThat(exchange.field("client_secret")).isEqualTo("test-google-secret");
        assertThat(exchange.field("redirect_uri")).isEqualTo("https://api.example.com/api/auth/google/desktop/callback");

        Api.Response tokens = finish(query(app).getFirst("code"), started.path("verifier").asString());

        assertThat(tokens.status()).as(tokens.body()).isEqualTo(200);
        assertThat(tokens.json().path("refreshToken").asString()).isNotBlank();
        JsonNode account = api.as(tokens.json().path("jwt").asString()).get("/api/v2/me").json();
        assertThat(account.path("email").asString()).isEqualTo(email);
        assertThat(account.path("firstName").asString()).isEqualTo("Ada");
        assertThat(account.path("role").asString()).isEqualTo("ROLE_USER");
    }

    @Test
    void itIsTheSameAccountAsFromTheOtherApps() {
        SignedIn phone = newUser();
        phone.api().post("/api/v2/me/favorites", Map.of("kind", "LINE", "lineId", "C01371"));
        JsonNode started = start();
        googleSignsIn(started, phone.googleSub(), "same-" + UUID.randomUUID() + "@example.com", "Ada");

        String code = query(browserComesBack(started, "google-code").header("Location")).getFirst("code");
        Api desktop = api.as(finish(code, started.path("verifier").asString()).json().path("jwt").asString());

        assertThat(desktop.get("/api/v2/me/favorites").json()).hasSize(1);
    }

    @Test
    void anAdminIsOneOnDesktopToo() {
        JsonNode started = start();
        googleSignsIn(started, UUID.randomUUID().toString(), "admin@example.com", "Admin");

        String code = query(browserComesBack(started, "google-code").header("Location")).getFirst("code");
        Api desktop = api.as(finish(code, started.path("verifier").asString()).json().path("jwt").asString());

        assertThat(desktop.get("/api/admin/status").status()).isEqualTo(200);
    }

    @Test
    void theCodeWorksOnceAndOnlyWithItsVerifier() {
        JsonNode mine = start();
        JsonNode someoneElse = start();
        googleSignsIn(mine, UUID.randomUUID().toString(), "once-" + UUID.randomUUID() + "@example.com", "Ada");
        String code = query(browserComesBack(mine, "google-code").header("Location")).getFirst("code");

        // Whoever sees the code go by in the browser has no verifier, and trying burns the code
        Api.Response stolen = finish(code, someoneElse.path("verifier").asString());

        assertThat(stolen.status()).isEqualTo(401);
        assertThat(finish(code, mine.path("verifier").asString()).status()).isEqualTo(401);
        assertThat(finish("made-up", mine.path("verifier").asString()).status()).isEqualTo(401);
        assertThat(api.post("/api/auth/google/desktop/token", "{}").status()).isEqualTo(401);
    }

    @Test
    void anIdTokenOfAnotherSignInIsRefused() {
        JsonNode started = start();
        // What Google would give for a code obtained in another sign-in: another nonce
        EXTERNAL.on("/google/token", idToken(GOOGLE.idToken("1", "x@example.com", "X",
            claims -> claims.claim("nonce", "another-sign-in"))));

        String app = browserComesBack(started, "code-of-another-sign-in").header("Location");

        assertThat(app).isEqualTo("http://127.0.0.1:" + APP_PORT + "/?error=failed&state=" + started.path("state").asString());
    }

    @Test
    void aRefusalOrAFailureAtGoogleGoesBackToTheApp() {
        JsonNode refused = start();
        JsonNode failed = start();
        EXTERNAL.on("/google/token", Response.json(400, "{\"error\": \"invalid_grant\"}"));

        Api.Response back = api.get("/api/auth/google/desktop/callback?error=access_denied&state=" + refused.path("state").asString());

        assertThat(back.status()).isEqualTo(302);
        assertThat(back.header("Location")).isEqualTo("http://127.0.0.1:" + APP_PORT + "/?error=access_denied&state="
            + refused.path("state").asString());
        assertThat(query(browserComesBack(failed, "refused-code").header("Location")).getFirst("error")).isEqualTo("failed");
    }

    @Test
    void anUnknownSignInOrPortIsRefused() {
        Api.Response unknown = api.get("/api/auth/google/desktop/callback?code=google-code&state=made-up");

        assertThat(unknown.status()).isEqualTo(400);
        assertThat(unknown.json().path("detail").asString()).isEqualTo("This sign-in has expired: start again from the app");
        assertThat(api.get("/api/auth/google/desktop/callback").status()).isEqualTo(400);
        assertThat(api.post("/api/auth/google/desktop", Map.of("port", 80)).status()).isEqualTo(400);
        assertThat(api.post("/api/auth/google/desktop", "{}").status()).isEqualTo(400);
    }

    @Test
    void guessingCodesIsRateLimitedPerAddressLikeTheOtherSignIns() {
        for (int i = 0; i < 10; i++) {
            assertThat(finish("guess-" + i, "guess").status()).isEqualTo(401);
        }

        assertThat(finish("one-more", "guess").status()).isEqualTo(429);
        assertThat(api.post("/api/auth/google/desktop", Map.of("port", APP_PORT)).status()).isEqualTo(429);
        assertThat(api.get("/api/auth/google/desktop/callback?code=google-code&state=made-up").status()).isEqualTo(429);
    }

    private JsonNode start() {
        Api.Response started = api.post("/api/auth/google/desktop", Map.of("port", APP_PORT));
        assertThat(started.status()).as(started.body()).isEqualTo(200);
        return started.json();
    }

    /** Google's token endpoint gives the ID token of this account, issued for the sign-in [started] */
    private static void googleSignsIn(JsonNode started, String sub, String email, String firstName) {
        String nonce = query(started.path("authorizationUrl").asString()).getFirst("nonce");
        EXTERNAL.on("/google/token", idToken(GOOGLE.idToken(sub, email, firstName, claims -> claims.claim("nonce", nonce))));
    }

    /** The answer of Google's token endpoint */
    private static Response idToken(String idToken) {
        return Response.json("{\"access_token\": \"unused\", \"id_token\": \"" + idToken + "\"}");
    }

    /** The browser, sent back by Google's page */
    private Api.Response browserComesBack(JsonNode started, String googleCode) {
        return api.get("/api/auth/google/desktop/callback?code=" + googleCode + "&state=" + started.path("state").asString());
    }

    private Api.Response finish(String code, String verifier) {
        return api.post("/api/auth/google/desktop/token", Map.of("code", code, "verifier", verifier));
    }

    private static MultiValueMap<String, String> query(String url) {
        return UriComponentsBuilder.fromUriString(url).build().getQueryParams();
    }
}
