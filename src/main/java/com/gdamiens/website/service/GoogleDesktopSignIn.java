package com.gdamiens.website.service;

import com.gdamiens.website.client.GoogleOAuthClient;
import com.gdamiens.website.controller.object.DesktopSignInStarted;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.service.GoogleIdTokenVerifier.GoogleAccount;
import com.gdamiens.website.utils.OpaqueTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sign-in with Google for the desktop apps (Linux, Windows), which have no Google SDK. In three steps:
 * <ol>
 * <li>{@link #start}: the app, listening on a port of the loopback, gets the address of Google's page (opened in the
 * system browser) and a secret of its own, the verifier;</li>
 * <li>{@link #callback}: Google sends the browser back here with a code, exchanged for the account's ID token (the web
 * client's secret stays on the server); the browser is then sent to the app's port with a one-time code;</li>
 * <li>{@link #redeem}: the app gives the one-time code and its verifier, and is signed in.</li>
 * </ol>
 * Whoever starts a sign-in and has someone else go through Google's page gets nothing: the one-time code only goes to
 * the loopback of the machine whose browser signed in, and is worth nothing without the verifier, which the browser
 * never sees. Sign-ins in progress are kept in memory: a restart forgets them, the user starts again.
 */
@Service
public class GoogleDesktopSignIn {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDesktopSignIn.class);

    /** Time to choose an account on Google's page (password, second factor) */
    static final Duration SIGN_IN_TIME = Duration.ofMinutes(10);

    /** The app exchanges the one-time code as soon as its port gets it */
    static final Duration CODE_TIME = Duration.ofMinutes(1);

    /** Anyone can start sign-ins (within the auth endpoints' rate limit per address): no more than this many at once */
    static final int MAX_IN_PROGRESS = 1000;

    /** Ports an app can listen on without privileges */
    private static final int MIN_PORT = 1024;

    private static final int MAX_PORT = 65535;

    /** Waiting for Google to send the browser back */
    private record Pending(int port, String nonce, String verifierHash, Instant expiresAt) {
    }

    /** Waiting for the app to come with the one-time code */
    private record Granted(GoogleAccount account, String verifierHash, Instant expiresAt) {
    }

    private final GoogleOAuthClient oauthClient;

    private final GoogleIdTokenVerifier idTokenVerifier;

    private final Clock clock;

    /** By state */
    private final Map<String, Pending> pending = new ConcurrentHashMap<>();

    /** By hash of the one-time code */
    private final Map<String, Granted> granted = new ConcurrentHashMap<>();

    public GoogleDesktopSignIn(GoogleOAuthClient oauthClient, GoogleIdTokenVerifier idTokenVerifier, Clock clock) {
        this.oauthClient = oauthClient;
        this.idTokenVerifier = idTokenVerifier;
        this.clock = clock;
    }

    /**
     * @param port port of the loopback the app listens on
     * @throws CustomException 503 when the web client's secret is not configured or too many sign-ins are in progress,
     *                         400 for a port no app can listen on
     */
    public DesktopSignInStarted start(Integer port) {
        if (!oauthClient.isConfigured()) {
            throw new CustomException("Sign-in with Google is not configured for desktop apps", HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (port == null || port < MIN_PORT || port > MAX_PORT) {
            throw new CustomException("port must be between " + MIN_PORT + " and " + MAX_PORT, HttpStatus.BAD_REQUEST);
        }
        Instant now = clock.instant();
        pending.values().removeIf(login -> !login.expiresAt().isAfter(now));
        granted.values().removeIf(grant -> !grant.expiresAt().isAfter(now));
        if (pending.size() >= MAX_IN_PROGRESS) {
            throw new CustomException("Too many sign-ins in progress, try again in a few minutes", HttpStatus.SERVICE_UNAVAILABLE);
        }
        String state = OpaqueTokens.generate("");
        String nonce = OpaqueTokens.generate("");
        String verifier = OpaqueTokens.generate("");
        pending.put(state, new Pending(port, nonce, OpaqueTokens.hash(verifier), now.plus(SIGN_IN_TIME)));
        return new DesktopSignInStarted(oauthClient.authorizationUrl(state, nonce), state, verifier);
    }

    /**
     * Google sent the browser back: with a code when the user chose an account, with an error otherwise.
     *
     * @return where to send the browser: the app's port, with the one-time code or with {@code error}
     *         ({@code access_denied} when the user refused, {@code failed} otherwise)
     * @throws CustomException 400 when this sign-in is unknown, expired or already came back
     */
    public URI callback(String state, String code, String error) {
        Pending login = state == null ? null : pending.remove(state);
        Instant now = clock.instant();
        if (login == null || !login.expiresAt().isAfter(now)) {
            throw new CustomException("This sign-in has expired: start again from the app", HttpStatus.BAD_REQUEST);
        }
        if (code == null || code.isBlank()) {
            return appUri(login.port(), state, "error", "access_denied".equals(error) ? "access_denied" : "failed");
        }
        GoogleAccount account;
        try {
            account = idTokenVerifier.verify(oauthClient.exchange(code), login.nonce());
        } catch (CustomException | RestClientException e) {
            // Never the code nor the account: only why (an invalid ID token is explained by the verifier's own log)
            LOGGER.info("Google desktop sign-in refused: {}", e instanceof CustomException ? e.getMessage() : e.getClass().getSimpleName());
            return appUri(login.port(), state, "error", "failed");
        }
        String oneTimeCode = OpaqueTokens.generate("");
        granted.put(OpaqueTokens.hash(oneTimeCode), new Granted(account, login.verifierHash(), now.plus(CODE_TIME)));
        return appUri(login.port(), state, "code", oneTimeCode);
    }

    /**
     * @return the Google account that signed in, once: the code is used up, even when the verifier is wrong
     * @throws CustomException 401 when the code is unknown, expired or already used, or the verifier not this sign-in's
     */
    public GoogleAccount redeem(String code, String verifier) {
        Granted grant = code == null ? null : granted.remove(OpaqueTokens.hash(code));
        if (grant == null || !grant.expiresAt().isAfter(clock.instant()) || verifier == null
            || !sameHash(OpaqueTokens.hash(verifier), grant.verifierHash())) {
            throw new CustomException("Invalid or expired sign-in code", HttpStatus.UNAUTHORIZED);
        }
        return grant.account();
    }

    /** Only ever the loopback: nothing here redirects to an address a caller gives */
    private static URI appUri(int port, String state, String name, String value) {
        return UriComponentsBuilder.fromUriString("http://127.0.0.1:" + port + "/")
            .queryParam(name, value)
            .queryParam("state", state)
            .build().toUri();
    }

    private static boolean sameHash(String one, String other) {
        return MessageDigest.isEqual(one.getBytes(StandardCharsets.US_ASCII), other.getBytes(StandardCharsets.US_ASCII));
    }
}
