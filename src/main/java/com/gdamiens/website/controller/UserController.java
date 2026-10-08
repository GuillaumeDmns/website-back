package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.Credentials;
import com.gdamiens.website.controller.object.GoogleSignInRequest;
import com.gdamiens.website.controller.object.JwtDTO;
import com.gdamiens.website.controller.object.RefreshTokenRequest;
import com.gdamiens.website.controller.object.SignUpRequest;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.security.UsageBudgets;
import com.gdamiens.website.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    private final UsageBudgets usageBudgets;

    public UserController(UserService userService, UsageBudgets usageBudgets) {
        this.userService = userService;
        this.usageBudgets = usageBudgets;
    }

    @PostMapping("/signin")
    @Operation(summary = "Get an access JWT and a refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credentials are valid"),
            @ApiResponse(responseCode = "400", description = "Username / password not provided"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Server error")})
    public ResponseEntity<JwtDTO> login(@RequestBody Credentials credentials) {

        if (credentials.getUsername() == null || credentials.getPassword() == null) {
            return new ResponseEntity<>(new JwtDTO(null), HttpStatus.BAD_REQUEST);
        }

        return userService.signIn(credentials.getUsername(), credentials.getPassword())
            .map(ResponseEntity::ok)
            .orElse(new ResponseEntity<>(new JwtDTO(null), HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/signup")
    @Operation(summary = "Create a user account and sign it in")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account created, tokens returned"),
        @ApiResponse(responseCode = "400", description = "Invalid username, email or password"),
        @ApiResponse(responseCode = "409", description = "Username or email already used"),
        @ApiResponse(responseCode = "500", description = "Server error")})
    public ResponseEntity<JwtDTO> signUp(@RequestBody SignUpRequest request) {
        JwtDTO tokens = userService.signUp(request.username(), request.email(), request.password());
        return new ResponseEntity<>(tokens, HttpStatus.CREATED);
    }

    @PostMapping("/auth/google")
    @Operation(summary = "Sign in with Google: exchange a Google ID token for an access JWT and a refresh token (account created at the first sign-in)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Signed in"),
        @ApiResponse(responseCode = "400", description = "ID token not provided"),
        @ApiResponse(responseCode = "401", description = "Invalid Google ID token (signature, issuer, audience, expiry, unverified email)"),
        @ApiResponse(responseCode = "503", description = "Sign-in with Google is not configured")})
    public ResponseEntity<JwtDTO> signInWithGoogle(@RequestBody GoogleSignInRequest request) {
        return ResponseEntity.ok(userService.signInWithGoogle(request.idToken()));
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
    public ResponseEntity<JwtDTO> refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request.refreshToken() == null) {
            return ResponseEntity.badRequest().build();
        }

        return userService.refresh(request.refreshToken())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke a refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Refresh token revoked (or already invalid)"),
        @ApiResponse(responseCode = "400", description = "Refresh token not provided")})
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        if (request.refreshToken() == null) {
            return ResponseEntity.badRequest().build();
        }

        userService.signOut(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ProblemDetail> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(e.toProblemDetail());
    }

}
