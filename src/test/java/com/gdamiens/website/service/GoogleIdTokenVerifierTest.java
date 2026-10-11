package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.support.FakeServer;
import com.gdamiens.website.support.FakeServer.Response;
import com.gdamiens.website.support.GoogleTokens;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleIdTokenVerifierTest {

    private static final FakeServer GOOGLE = FakeServer.start();

    private static final GoogleTokens TOKENS = new GoogleTokens();

    private static CloseableHttpClient httpClient;

    private GoogleIdTokenVerifier verifier;

    @BeforeAll
    static void serveKeys() {
        GOOGLE.on("/certs", Response.json(TOKENS.jwks()));
        httpClient = new HttpClientConfig().httpClient();
    }

    @AfterAll
    static void stop() throws Exception {
        GOOGLE.close();
        httpClient.close();
    }

    @BeforeEach
    void setUp() {
        verifier = verifier(List.of(GoogleTokens.CLIENT_ID));
    }

    @Test
    void readsAValidToken() {
        GoogleIdTokenVerifier.GoogleAccount account = verifier.verify(TOKENS.idToken("123", "ada@example.com", "Ada"));

        assertThat(account).isEqualTo(new GoogleIdTokenVerifier.GoogleAccount("123", "ada@example.com", "Ada"));
    }

    @Test
    void fallsBackToTheFullNameWithoutFirstName() {
        String token = TOKENS.idToken("123", "ada@example.com", "Ada", claims -> claims.claim("given_name", null));

        assertThat(verifier.verify(token).firstName()).isEqualTo("Ada Test");
    }

    @Test
    void refusesAnotherAudience() {
        String token = TOKENS.idToken("123", "ada@example.com", "Ada", claims -> claims.audience("someone-else"));

        assertUnauthorized(token);
    }

    @Test
    void refusesAnotherIssuer() {
        String token = TOKENS.idToken("123", "ada@example.com", "Ada", claims -> claims.issuer("https://evil.example.com"));

        assertUnauthorized(token);
    }

    @Test
    void refusesAnExpiredToken() {
        String token = TOKENS.idToken("123", "ada@example.com", "Ada",
            claims -> claims.expirationTime(Date.from(Instant.now().minus(Duration.ofHours(2)))));

        assertUnauthorized(token);
    }

    @Test
    void refusesAnUnverifiedEmail() {
        String token = TOKENS.idToken("123", "ada@example.com", "Ada", claims -> claims.claim("email_verified", false));

        assertUnauthorized(token);
    }

    @Test
    void refusesAnotherSignature() {
        assertUnauthorized(TOKENS.forgedIdToken("123", "ada@example.com"));
    }

    @Test
    void refusesGarbage() {
        assertUnauthorized("not.a.token");
    }

    @Test
    void refusesAMissingToken() {
        assertThatThrownBy(() -> verifier.verify(" "))
            .isInstanceOfSatisfying(CustomException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void isDisabledWithoutClientIds() {
        GoogleIdTokenVerifier disabled = verifier(List.of());

        assertThat(disabled.isEnabled()).isFalse();
        assertThatThrownBy(() -> disabled.verify(TOKENS.idToken("123", "ada@example.com", "Ada")))
            .isInstanceOfSatisfying(CustomException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    private void assertUnauthorized(String token) {
        assertThatThrownBy(() -> verifier.verify(token))
            .isInstanceOfSatisfying(CustomException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    private static GoogleIdTokenVerifier verifier(List<String> clientIds) {
        ApplicationProperties properties = new ApplicationProperties();
        properties.getGoogle().setClientIds(clientIds);
        properties.getGoogle().setJwkSetUri(GOOGLE.url() + "/certs");
        return new GoogleIdTokenVerifier(properties, httpClient);
    }
}
