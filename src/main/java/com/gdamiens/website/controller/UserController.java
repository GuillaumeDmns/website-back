package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.Credentials;
import com.gdamiens.website.controller.object.JwtDTO;
import com.gdamiens.website.controller.object.RefreshTokenRequest;
import com.gdamiens.website.controller.object.SignUpRequest;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.security.Role;
import com.gdamiens.website.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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

    @GetMapping("/refresh")
    @Operation(summary = "Refresh the access JWT from a still valid one (legacy, prefer /api/token/refresh)", security = @SecurityRequirement(name = "Auth. Token"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "JWT refreshed"),
        @ApiResponse(responseCode = "401", description = "Invalid JWT"),
        @ApiResponse(responseCode = "500", description = "Server error")})
    public ResponseEntity<JwtDTO> refresh(HttpServletRequest req) {

        return userService.refreshAccessToken(req.getRemoteUser())
            .map(token -> ResponseEntity.ok(new JwtDTO(token)))
            .orElse(new ResponseEntity<>(new JwtDTO(null), HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user", security = @SecurityRequirement(name = "Auth. Token"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created"),
        @ApiResponse(responseCode = "400", description = "Username / password not provided"),
        @ApiResponse(responseCode = "403", description = "Not authorized (admin only)"),
        @ApiResponse(responseCode = "409", description = "Login already exists"),
        @ApiResponse(responseCode = "500", description = "Server error")})
    public ResponseEntity<Void> createUser(@RequestBody Credentials credentials) {

        if (credentials.getUsername() == null || credentials.getPassword() == null) {
            return ResponseEntity.badRequest().build();
        }

        return userService.createUser(credentials.getUsername(), credentials.getPassword(), Role.ROLE_USER)
            .map(user -> new ResponseEntity<Void>(HttpStatus.CREATED))
            .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ProblemDetail> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getHttpStatus())
            .body(ProblemDetail.forStatusAndDetail(e.getHttpStatus(), e.getMessage()));
    }

}
