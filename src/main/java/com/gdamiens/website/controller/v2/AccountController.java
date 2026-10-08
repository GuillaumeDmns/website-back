package com.gdamiens.website.controller.v2;

import com.gdamiens.website.controller.object.v2.Account;
import com.gdamiens.website.controller.object.v2.AccountExport;
import com.gdamiens.website.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/** The signed-in user's account: profile, export of its data, deletion */
@RestController
@RequestMapping("/api/v2/me")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
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

    @DeleteMapping
    @Operation(summary = "Delete the account for good, with its favorites and sessions", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> delete(Principal principal) {
        userService.deleteAccount(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
