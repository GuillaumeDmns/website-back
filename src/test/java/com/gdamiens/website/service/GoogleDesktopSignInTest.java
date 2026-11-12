package com.gdamiens.website.service;

import com.gdamiens.website.client.GoogleOAuthClient;
import com.gdamiens.website.controller.object.DesktopSignInStarted;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.service.GoogleIdTokenVerifier.GoogleAccount;
import com.gdamiens.website.support.MutableClock;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The desktop apps' sign-in through the browser: who gets the account, once, and for how long */
class GoogleDesktopSignInTest {

    private static final int PORT = 51234;

    private static final GoogleAccount ADA = new GoogleAccount("123", "ada@example.com", "Ada");

    private final GoogleOAuthClient oauthClient = mock(GoogleOAuthClient.class);

    private final GoogleIdTokenVerifier idTokenVerifier = mock(GoogleIdTokenVerifier.class);

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-12T10:00:00Z"));

    private GoogleDesktopSignIn signIn;

    @BeforeEach
    void setUp() {
        signIn = new GoogleDesktopSignIn(oauthClient, idTokenVerifier, clock);
        when(oauthClient.isConfigured()).thenReturn(true);
        when(oauthClient.authorizationUrl(anyString(), anyString())).thenReturn("https://accounts.example.com/auth");
        when(oauthClient.exchange("google-code")).thenReturn("id-token");
        when(idTokenVerifier.verify(eq("id-token"), anyString())).thenReturn(ADA);
    }

    @Test
    void startsWithGooglesPageAStateAndAVerifier() {
        DesktopSignInStarted started = signIn.start(PORT);
        DesktopSignInStarted other = signIn.start(PORT);

        assertThat(started.authorizationUrl()).isEqualTo("https://accounts.example.com/auth");
        verify(oauthClient).authorizationUrl(eq(started.state()), anyString());
        // 256 random bits each, in base64url
        assertThat(started.state()).hasSize(43).isNotEqualTo(other.state()).isNotEqualTo(started.verifier());
        assertThat(started.verifier()).hasSize(43).isNotEqualTo(other.verifier());
    }

    @Test
    void theCodeBroughtToTheAppsPortSignsItIn() {
        DesktopSignInStarted started = signIn.start(PORT);

        URI app = signIn.callback(started.state(), "google-code", null);

        assertThat(app.toString()).startsWith("http://127.0.0.1:" + PORT + "/?code=");
        assertThat(param(app, "state")).isEqualTo(started.state());
        assertThat(signIn.redeem(param(app, "code"), started.verifier())).isEqualTo(ADA);
    }

    @Test
    void theIdTokenMustBeTheOneOfThisSignIn() {
        DesktopSignInStarted started = signIn.start(PORT);
        ArgumentCaptor<String> askedNonce = ArgumentCaptor.forClass(String.class);
        verify(oauthClient).authorizationUrl(eq(started.state()), askedNonce.capture());

        signIn.callback(started.state(), "google-code", null);

        verify(idTokenVerifier).verify("id-token", askedNonce.getValue());
    }

    @Test
    void theCodeWorksOnce() {
        DesktopSignInStarted started = signIn.start(PORT);
        String code = param(signIn.callback(started.state(), "google-code", null), "code");
        signIn.redeem(code, started.verifier());

        assertRefused(() -> signIn.redeem(code, started.verifier()), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void theCodeIsWorthNothingWithoutTheVerifierAndIsUsedUp() {
        DesktopSignInStarted started = signIn.start(PORT);
        DesktopSignInStarted someoneElse = signIn.start(PORT);
        String code = param(signIn.callback(started.state(), "google-code", null), "code");

        assertRefused(() -> signIn.redeem(code, someoneElse.verifier()), HttpStatus.UNAUTHORIZED);
        assertRefused(() -> signIn.redeem(code, null), HttpStatus.UNAUTHORIZED);
        assertRefused(() -> signIn.redeem(code, started.verifier()), HttpStatus.UNAUTHORIZED);
        assertRefused(() -> signIn.redeem(null, started.verifier()), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void theCodeLastsAMinute() {
        DesktopSignInStarted started = signIn.start(PORT);
        String code = param(signIn.callback(started.state(), "google-code", null), "code");

        clock.advance(GoogleDesktopSignIn.CODE_TIME);

        assertRefused(() -> signIn.redeem(code, started.verifier()), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void googleHasTenMinutesToComeBack() {
        DesktopSignInStarted intime = signIn.start(PORT);
        DesktopSignInStarted late = signIn.start(PORT);

        clock.advance(GoogleDesktopSignIn.SIGN_IN_TIME.minusSeconds(1));
        assertThat(param(signIn.callback(intime.state(), "google-code", null), "code")).isNotBlank();
        clock.advance(Duration.ofSeconds(1));

        assertRefused(() -> signIn.callback(late.state(), "google-code", null), HttpStatus.BAD_REQUEST);
        verify(oauthClient).exchange(anyString());
    }

    @Test
    void googleComesBackOnce() {
        DesktopSignInStarted started = signIn.start(PORT);
        signIn.callback(started.state(), "google-code", null);

        assertRefused(() -> signIn.callback(started.state(), "google-code", null), HttpStatus.BAD_REQUEST);
        assertRefused(() -> signIn.callback("made-up", "google-code", null), HttpStatus.BAD_REQUEST);
        assertRefused(() -> signIn.callback(null, "google-code", null), HttpStatus.BAD_REQUEST);
    }

    @Test
    void aRefusalAtGoogleGoesBackToTheApp() {
        DesktopSignInStarted refused = signIn.start(PORT);
        DesktopSignInStarted broken = signIn.start(PORT);

        URI app = signIn.callback(refused.state(), null, "access_denied");

        assertThat(app).hasToString("http://127.0.0.1:" + PORT + "/?error=access_denied&state=" + refused.state());
        // Whatever else Google says is not repeated to the loopback
        assertThat(param(signIn.callback(broken.state(), null, "<script>"), "error")).isEqualTo("failed");
        verify(oauthClient, never()).exchange(any());
    }

    @Test
    void aCodeGoogleRefusesGoesBackToTheAppAsAFailure() {
        when(oauthClient.exchange("used-code")).thenThrow(new ResourceAccessException("400"));
        when(oauthClient.exchange("other-sign-in")).thenReturn("other-id-token");
        when(idTokenVerifier.verify(eq("other-id-token"), anyString()))
            .thenThrow(new CustomException("Invalid Google ID token", HttpStatus.UNAUTHORIZED));
        DesktopSignInStarted first = signIn.start(PORT);
        DesktopSignInStarted second = signIn.start(PORT);

        assertThat(param(signIn.callback(first.state(), "used-code", null), "error")).isEqualTo("failed");
        URI app = signIn.callback(second.state(), "other-sign-in", null);

        assertThat(param(app, "error")).isEqualTo("failed");
        assertThat(param(app, "code")).isNull();
    }

    @Test
    void refusesToStartWithoutTheWebClientsSecret() {
        when(oauthClient.isConfigured()).thenReturn(false);

        assertRefused(() -> signIn.start(PORT), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void refusesAPortNoAppListensOn() {
        assertRefused(() -> signIn.start(null), HttpStatus.BAD_REQUEST);
        assertRefused(() -> signIn.start(443), HttpStatus.BAD_REQUEST);
        assertRefused(() -> signIn.start(65536), HttpStatus.BAD_REQUEST);
        assertThat(signIn.start(1024)).isNotNull();
        assertThat(signIn.start(65535)).isNotNull();
    }

    @Test
    void keepsNoMoreSignInsThanItsLimitUntilTheyExpire() {
        for (int i = 0; i < GoogleDesktopSignIn.MAX_IN_PROGRESS; i++) {
            signIn.start(PORT);
        }

        assertRefused(() -> signIn.start(PORT), HttpStatus.SERVICE_UNAVAILABLE);
        clock.advance(GoogleDesktopSignIn.SIGN_IN_TIME);

        assertThat(signIn.start(PORT)).isNotNull();
    }

    private static String param(URI uri, String name) {
        return UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst(name);
    }

    private static void assertRefused(ThrowingCallable call, HttpStatus status) {
        assertThatThrownBy(call)
            .isInstanceOfSatisfying(CustomException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(status));
    }
}
