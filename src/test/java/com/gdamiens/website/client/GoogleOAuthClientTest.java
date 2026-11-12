package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import com.gdamiens.website.support.FakeServer;
import com.gdamiens.website.support.FakeServer.Response;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleOAuthClientTest {

    private static final String REDIRECT_URI = "https://api.example.com/api/auth/google/desktop/callback";

    private static final FakeServer GOOGLE = FakeServer.start();

    private static CloseableHttpClient httpClient;

    private ApplicationProperties properties;

    @BeforeAll
    static void start() {
        httpClient = new HttpClientConfig().httpClient();
    }

    @AfterAll
    static void stop() throws Exception {
        GOOGLE.close();
        httpClient.close();
    }

    @BeforeEach
    void setUp() {
        GOOGLE.reset();
        properties = new ApplicationProperties();
        properties.getGoogle().setAuthorizationUri(GOOGLE.url() + "/auth");
        properties.getGoogle().setTokenUri(GOOGLE.url() + "/token");
        properties.getGoogle().getDesktop().setClientId("web-client");
        properties.getGoogle().getDesktop().setClientSecret("web-secret");
        properties.getGoogle().getDesktop().setRedirectUri(REDIRECT_URI);
    }

    @Test
    void googlesPageAsksForTheAccountOfTheWebClientAndComesBackHere() {
        String url = client().authorizationUrl("the-state", "the-nonce");

        assertThat(url).startsWith(GOOGLE.url() + "/auth?");
        // Encoded as a whole: Google compares it with the registered one after decoding
        assertThat(url).contains("redirect_uri=https%3A%2F%2Fapi.example.com%2Fapi%2Fauth%2Fgoogle%2Fdesktop%2Fcallback")
            .contains("scope=openid%20email%20profile");
        MultiValueMap<String, String> params = UriComponentsBuilder.fromUriString(url).build().getQueryParams();
        assertThat(params.getFirst("client_id")).isEqualTo("web-client");
        assertThat(params.getFirst("response_type")).isEqualTo("code");
        assertThat(params.getFirst("state")).isEqualTo("the-state");
        assertThat(params.getFirst("nonce")).isEqualTo("the-nonce");
        assertThat(params.getFirst("prompt")).isEqualTo("select_account");
        assertThat(url).doesNotContain("web-secret");
    }

    @Test
    void exchangesTheCodeForTheIdTokenWithTheSecret() {
        GOOGLE.on("/token", Response.json("{\"access_token\": \"unused\", \"id_token\": \"the-id-token\", \"expires_in\": 3599}"));

        assertThat(client().exchange("4/google code")).isEqualTo("the-id-token");

        FakeServer.Request sent = GOOGLE.requests("/token").getFirst();
        assertThat(sent.method()).isEqualTo("POST");
        assertThat(sent.field("grant_type")).isEqualTo("authorization_code");
        assertThat(sent.field("code")).isEqualTo("4/google code");
        assertThat(sent.field("client_id")).isEqualTo("web-client");
        assertThat(sent.field("client_secret")).isEqualTo("web-secret");
        assertThat(sent.field("redirect_uri")).isEqualTo(REDIRECT_URI);
    }

    @Test
    void aRefusedCodeIsAFailure() {
        GOOGLE.on("/token", Response.json(400, "{\"error\": \"invalid_grant\"}"));

        assertThatThrownBy(() -> client().exchange("used")).isInstanceOf(RestClientException.class);
    }

    @Test
    void anAnswerWithoutIdTokenGivesNone() {
        GOOGLE.on("/token", Response.json("{\"access_token\": \"only\"}"));

        assertThat(client().exchange("code")).isNull();
    }

    @Test
    void needsTheClientItsSecretAndTheRedirectUri() {
        assertThat(client().isConfigured()).isTrue();

        properties.getGoogle().getDesktop().setClientSecret(" ");
        assertThat(client().isConfigured()).isFalse();

        assertThat(new GoogleOAuthClient(new ApplicationProperties(), httpClient).isConfigured()).isFalse();
    }

    private GoogleOAuthClient client() {
        return new GoogleOAuthClient(properties, httpClient);
    }
}
