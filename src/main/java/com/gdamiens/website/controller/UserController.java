package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.ApiTokenRequest;
import com.gdamiens.website.controller.object.DesktopSignInCodeRequest;
import com.gdamiens.website.controller.object.DesktopSignInRequest;
import com.gdamiens.website.controller.object.DesktopSignInStarted;
import com.gdamiens.website.controller.object.GoogleSignInRequest;
import com.gdamiens.website.controller.object.JwtDTO;
import com.gdamiens.website.controller.object.RefreshTokenRequest;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.security.SecurityEvents;
import com.gdamiens.website.security.UsageBudgets;
import com.gdamiens.website.service.GoogleDesktopSignIn;
import com.gdamiens.website.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    private final UsageBudgets usageBudgets;

    private final GoogleDesktopSignIn googleDesktopSignIn;

    private final SecurityEvents securityEvents;

    public UserController(UserService userService, UsageBudgets usageBudgets, GoogleDesktopSignIn googleDesktopSignIn,
                          SecurityEvents securityEvents) {
        this.userService = userService;
        this.usageBudgets = usageBudgets;
        this.googleDesktopSignIn = googleDesktopSignIn;
        this.securityEvents = securityEvents;
    }

    @PostMapping("/auth/api-token")
    @Operation(summary = "Exchange a personal API token (Bruno, scripts; created by an admin at /api/v2/me/api-tokens) for an access JWT, without refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Access JWT of the token's account"),
        @ApiResponse(responseCode = "401", description = "Unknown or expired API token")})
    public JwtDTO signInWithApiToken(@RequestBody ApiTokenRequest request) {
        return userService.signInWithApiToken(request.token())
            .orElseThrow(() -> refused(SecurityEvents.Kind.API_TOKEN_REFUSED, "Unknown or expired API token"));
    }

    @PostMapping("/auth/google")
    @Operation(summary = "Sign in with Google: exchange a Google ID token for an access JWT and a refresh token (account created at the first sign-in)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Signed in"),
        @ApiResponse(responseCode = "400", description = "ID token not provided"),
        @ApiResponse(responseCode = "401", description = "Invalid Google ID token (signature, issuer, audience, expiry, unverified email)"),
        @ApiResponse(responseCode = "503", description = "Sign-in with Google is not configured")})
    public ResponseEntity<JwtDTO> signInWithGoogle(@RequestBody GoogleSignInRequest request) {
        try {
            return ResponseEntity.ok(userService.signInWithGoogle(request.idToken()));
        } catch (CustomException e) {
            if (e.getHttpStatus() == HttpStatus.UNAUTHORIZED) {
                securityEvents.record(SecurityEvents.Kind.GOOGLE_TOKEN_REFUSED);
            }
            throw e;
        }
    }

    @PostMapping("/auth/google/desktop")
    @Operation(summary = "Desktop apps (Linux, Windows), step 1: start a sign-in with Google through the system browser; the app listens on a port of the loopback and opens the address given")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Google's page to open, the state that will come back to the app's port, and the verifier to send with the one-time code"),
        @ApiResponse(responseCode = "400", description = "Port missing or not between 1024 and 65535"),
        @ApiResponse(responseCode = "503", description = "Not configured (no secret for the web client), or too many sign-ins in progress")})
    public DesktopSignInStarted startDesktopSignIn(@RequestBody DesktopSignInRequest request) {
        return googleDesktopSignIn.start(request.port());
    }

    @GetMapping("/auth/google/desktop/callback")
    @Operation(summary = "Desktop apps, step 2 (the browser, sent back by Google): redirects to the app's port, http://127.0.0.1:<port>/?code=<one-time code>&state=… or ?error=access_denied|failed&state=…")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "To the app's port on the loopback"),
        @ApiResponse(responseCode = "400", description = "Sign-in unknown, expired (10 min) or already come back")})
    public ResponseEntity<Void> desktopSignInCallback(@RequestParam(required = false) String state,
                                                      @RequestParam(required = false) String code,
                                                      @RequestParam(required = false) String error) {
        return ResponseEntity.status(HttpStatus.FOUND).location(googleDesktopSignIn.callback(state, code, error)).build();
    }

    @PostMapping("/auth/google/desktop/token")
    @Operation(summary = "Desktop apps, step 3: exchange the one-time code received on the app's port, with the verifier of step 1, for an access JWT and a refresh token (as POST /api/auth/google)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Signed in"),
        @ApiResponse(responseCode = "401", description = "Code unknown, expired (1 min) or already used, or verifier of another sign-in")})
    public JwtDTO finishDesktopSignIn(@RequestBody DesktopSignInCodeRequest request) {
        return userService.signInWithGoogle(googleDesktopSignIn.redeem(request.code(), request.verifier()));
    }

    @PostMapping("/auth/guest")
    @Operation(summary = "Get the access token of a device used without account (capped usage, 30 days, no refresh token)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guest token returned"),
        @ApiResponse(responseCode = "429", description = "Too many guest tokens from this address")})
    public ResponseEntity<JwtDTO> guest(HttpServletRequest request) {
        usageBudgets.consumeGuestToken(request.getRemoteAddr());
        return ResponseEntity.ok(userService.createGuestToken());
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "Exchange a refresh token for a new access JWT and a new refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tokens renewed, the previous refresh token is no longer valid"),
        @ApiResponse(responseCode = "400", description = "Refresh token not provided"),
        @ApiResponse(responseCode = "401", description = "Invalid, expired or revoked refresh token"),
        @ApiResponse(responseCode = "500", description = "Server error")})
    public JwtDTO refreshToken(@RequestBody RefreshTokenRequest request) {
        return userService.refresh(requireRefreshToken(request))
            .orElseThrow(() -> refused(SecurityEvents.Kind.REFRESH_TOKEN_REFUSED, "Invalid, expired or revoked refresh token"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke a refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Refresh token revoked (or already invalid)"),
        @ApiResponse(responseCode = "400", description = "Refresh token not provided")})
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        userService.signOut(requireRefreshToken(request));
        return ResponseEntity.noContent().build();
    }

    /** A 401 for refused credentials, counted for the administration's security page */
    private CustomException refused(SecurityEvents.Kind kind, String message) {
        securityEvents.record(kind);
        return new CustomException(message, HttpStatus.UNAUTHORIZED);
    }

    private static String requireRefreshToken(RefreshTokenRequest request) {
        if (request.refreshToken() == null) {
            throw new CustomException("Refresh token is required", HttpStatus.BAD_REQUEST);
        }
        return request.refreshToken();
    }
}
