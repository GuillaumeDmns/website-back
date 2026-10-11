package com.gdamiens.website.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Google ID tokens signed by a key of the tests, whose public part {@link #jwks()} serves like Google's
 * {@code /oauth2/v3/certs}
 */
public final class GoogleTokens {

    public static final String CLIENT_ID = "test-client.apps.googleusercontent.com";

    private final RSAKey key;

    public GoogleTokens() {
        try {
            key = new RSAKeyGenerator(2048).keyID("test-key").generate();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    public String jwks() {
        return new JWKSet(key.toPublicJWK()).toString();
    }

    /** A valid ID token of [sub], then changed by [claims] */
    public String idToken(String sub, String email, String firstName, Consumer<JWTClaimsSet.Builder> claims) {
        Instant now = Instant.now();
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
            .issuer("https://accounts.google.com")
            .audience(CLIENT_ID)
            .subject(sub)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plus(Duration.ofHours(1))))
            .claim("email", email)
            .claim("email_verified", true)
            .claim("given_name", firstName)
            .claim("name", firstName + " Test");
        claims.accept(builder);
        return sign(builder.build(), key);
    }

    public String idToken(String sub, String email, String firstName) {
        return idToken(sub, email, firstName, builder -> {
        });
    }

    /** Signed by another key than the served one */
    public String forgedIdToken(String sub, String email) {
        try {
            RSAKey other = new RSAKeyGenerator(2048).keyID("test-key").generate();
            return sign(new JWTClaimsSet.Builder()
                .issuer("https://accounts.google.com")
                .audience(CLIENT_ID)
                .subject(sub)
                .expirationTime(Date.from(Instant.now().plus(Duration.ofHours(1))))
                .claim("email", email)
                .claim("email_verified", true)
                .build(), other);
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String sign(JWTClaimsSet claims, RSAKey key) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims);
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }
}
