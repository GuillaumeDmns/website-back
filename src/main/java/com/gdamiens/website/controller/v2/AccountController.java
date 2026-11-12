package com.gdamiens.website.controller.v2;

import com.gdamiens.website.controller.object.v2.Account;
import com.gdamiens.website.controller.object.v2.AccountExport;
import com.gdamiens.website.security.ApiTokenSessions;
import com.gdamiens.website.service.RefreshTokenService;
import com.gdamiens.website.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.Authentication;

import java.security.Principal;

/** The signed-in user's account: profile, export of its data, deletion */
@RestController
@RequestMapping("/api/v2/me")
public class AccountController {

    private final UserService userService;

    private final RefreshTokenService refreshTokenService;

    public AccountController(UserService userService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    /** @param active sessions still usable: one per device signed in (an API token's access is not one) */
    public record Sessions(long active) {
    }

    /** @param closed sessions closed, this one included */
    public record ClosedSessions(int closed) {
    }

    @GetMapping
    @Operation(summary = "The account: email, first name, role", security = @SecurityRequirement(name = "Auth. Token"))
    public Account get(Principal principal) {
        return userService.getAccount(principal.getName());
    }

    @GetMapping("/export")
    @Operation(summary = "Everything kept about the account (GDPR): the account and its favorites, as a JSON file",
        security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<AccountExport> export(Principal principal) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"account.json\"")
            .body(userService.exportAccount(principal.getName()));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Sessions of the account still usable (devices signed in)", security = @SecurityRequirement(name = "Auth. Token"))
    public Sessions sessions(Principal principal) {
        return new Sessions(refreshTokenService.countActive(userService.user(principal.getName())));
    }

    @DeleteMapping("/sessions")
    @Operation(summary = "Sign out everywhere: closes every session of the account, this one included (their access tokens end within the hour)",
        security = @SecurityRequirement(name = "Auth. Token"))
    public ClosedSessions closeSessions(Principal principal) {
        return new ClosedSessions(refreshTokenService.revokeAll(userService.user(principal.getName())));
    }

    @DeleteMapping
    @Operation(summary = "Delete the account for good, with its favorites and sessions (not with an API token: 403 api_token_session)",
        security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> delete(Authentication authentication) {
        ApiTokenSessions.requireOwnSession(authentication);
        userService.deleteAccount(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
