package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.util.Map;

/**
 * Google's OAuth 2.0 authorization code flow (OpenID Connect) for the web client of {@code application.google.desktop}:
 * the address of Google's sign-in page, and the exchange of the code Google gives back for an ID token. The only place
 * using the client's secret.
 */
@Component
public class GoogleOAuthClient {

    private final ApplicationProperties.Google google;

    private final RestTemplate restTemplate;

    public GoogleOAuthClient(ApplicationProperties applicationProperties, HttpClient httpClient) {
        this.google = applicationProperties.getGoogle();
        this.restTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.DEFAULT_READ_TIMEOUT));
    }

    public boolean isConfigured() {
        ApplicationProperties.Google.Desktop desktop = google.getDesktop();
        return StringUtils.isNoneBlank(desktop.getClientId(), desktop.getClientSecret(), desktop.getRedirectUri());
    }

    /**
     * Google's sign-in page, which always asks which account to use (the browser may know several).
     *
     * @param state comes back with the code
     * @param nonce comes back in the ID token
     */
    public String authorizationUrl(String state, String nonce) {
        return UriComponentsBuilder.fromUriString(google.getAuthorizationUri())
            .queryParam("client_id", "{clientId}")
            .queryParam("redirect_uri", "{redirectUri}")
            .queryParam("response_type", "code")
            .queryParam("scope", "{scope}")
            .queryParam("state", "{state}")
            .queryParam("nonce", "{nonce}")
            .queryParam("prompt", "select_account")
            .encode()
            .buildAndExpand(Map.of(
                "clientId", google.getDesktop().getClientId(),
                "redirectUri", google.getDesktop().getRedirectUri(),
                "scope", "openid email profile",
                "state", state,
                "nonce", nonce))
            .toUriString();
    }

    /**
     * @param code what Google gave the browser back with
     * @return the ID token of the account chosen, to be verified; null when Google's answer holds none
     * @throws RestClientException when Google refuses the code (already used, expired, of another client) or can't be reached
     */
    public String exchange(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", google.getDesktop().getClientId());
        form.add("client_secret", google.getDesktop().getClientSecret());
        form.add("redirect_uri", google.getDesktop().getRedirectUri());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        JsonNode answer = restTemplate.postForObject(google.getTokenUri(), new HttpEntity<>(form, headers), JsonNode.class);
        return answer == null ? null : answer.path("id_token").asString(null);
    }
}
