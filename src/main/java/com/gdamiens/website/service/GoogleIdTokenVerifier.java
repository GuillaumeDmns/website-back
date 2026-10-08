package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import com.gdamiens.website.exceptions.CustomException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

/**
 * Checks the ID tokens of "Sign in with Google" (OpenID Connect): signature with Google's public keys (fetched and
 * cached by Nimbus), issuer, audience (one of our OAuth client ids), expiry and a verified email. No Google secret on
 * the server, no Google token kept.
 */
@Service
public class GoogleIdTokenVerifier {

    private static final Set<String> ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    /** What the app needs from a Google account */
    public record GoogleAccount(String sub, String email, String firstName) {
    }

    private final List<String> clientIds;

    private final NimbusJwtDecoder decoder;

    public GoogleIdTokenVerifier(ApplicationProperties applicationProperties, HttpClient httpClient) {
        ApplicationProperties.Google google = applicationProperties.getGoogle();
        this.clientIds = List.copyOf(google.getClientIds());
        this.decoder = NimbusJwtDecoder.withJwkSetUri(google.getJwkSetUri())
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .restOperations(new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.DEFAULT_READ_TIMEOUT)))
            .build();
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            new JwtClaimValidator<String>("iss", ISSUERS::contains),
            new JwtClaimValidator<List<String>>("aud", audience -> audience != null && audience.stream().anyMatch(clientIds::contains)),
            new JwtClaimValidator<Boolean>("email_verified", Boolean.TRUE::equals));
        this.decoder.setJwtValidator(validator);
    }

    public boolean isEnabled() {
        return !clientIds.isEmpty();
    }

    /**
     * @throws CustomException 503 when no client id is configured, 401 when the token is not a valid Google ID token
     */
    public GoogleAccount verify(String idToken) {
        if (!isEnabled()) {
            throw new CustomException("Sign-in with Google is not configured", HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (idToken == null || idToken.isBlank()) {
            throw new CustomException("Missing Google ID token", HttpStatus.BAD_REQUEST);
        }
        Jwt token;
        try {
            token = decoder.decode(idToken);
        } catch (JwtException e) {
            throw new CustomException("Invalid Google ID token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
        String firstName = token.getClaimAsString("given_name");
        return new GoogleAccount(token.getSubject(), token.getClaimAsString("email"),
            firstName != null ? firstName : token.getClaimAsString("name"));
    }
}
