package com.gdamiens.website.controller.v2;

import com.gdamiens.website.controller.object.v2.ApiToken;
import com.gdamiens.website.controller.object.v2.ApiTokenCreation;
import com.gdamiens.website.controller.object.v2.NewApiToken;
import com.gdamiens.website.service.ApiTokenService;
import com.gdamiens.website.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/** Personal API tokens of the signed-in admin (Bruno, scripts) */
@RestController
@RequestMapping("/api/v2/me/api-tokens")
@PreAuthorize("hasRole('ADMIN')")
public class ApiTokenController {

    private final ApiTokenService apiTokenService;

    private final UserService userService;

    public ApiTokenController(ApiTokenService apiTokenService, UserService userService) {
        this.apiTokenService = apiTokenService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "API tokens of the admin, without their value", security = @SecurityRequirement(name = "Auth. Token"))
    public List<ApiToken> list(Principal principal) {
        return apiTokenService.list(userService.user(principal.getName()));
    }

    @PostMapping
    @Operation(summary = "Create an API token: its value is only given in this answer (at most 10 tokens, 365 days)",
        security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<NewApiToken> create(Principal principal, @RequestBody ApiTokenCreation request) {
        return new ResponseEntity<>(apiTokenService.create(userService.user(principal.getName()), request.name(), request.validDays()),
            HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revoke an API token", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> delete(Principal principal, @PathVariable long id) {
        apiTokenService.delete(userService.user(principal.getName()), id);
        return ResponseEntity.noContent().build();
    }
}
