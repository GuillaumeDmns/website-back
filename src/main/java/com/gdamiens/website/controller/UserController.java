package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.Credentials;
import com.gdamiens.website.controller.object.JwtDTO;
import com.gdamiens.website.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signin")
    @Operation(summary = "Get the JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credentials are valid"),
            @ApiResponse(responseCode = "400", description = "Username / password not provided"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Server error")})
    public ResponseEntity<JwtDTO> login(@RequestBody Credentials credentials) {

        if (credentials.getUsername() == null || credentials.getPassword() == null) {
            return new ResponseEntity<>(new JwtDTO(null), HttpStatus.BAD_REQUEST);
        }

        String token = userService.signIn(credentials.getUsername(), credentials.getPassword());

        return new ResponseEntity<>(new JwtDTO(token), token == null ? HttpStatus.UNAUTHORIZED : HttpStatus.OK);
    }


    @GetMapping("/refresh")
    @Operation(summary = "Refresh the JWT", security = @SecurityRequirement(name = "Auth. Token"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "JWT refreshed"),
        @ApiResponse(responseCode = "401", description = "Invalid JWT"),
        @ApiResponse(responseCode = "500", description = "Server error")})
    public ResponseEntity<JwtDTO> refresh(HttpServletRequest req) {

        String token = userService.refresh(req.getRemoteUser());
        return new ResponseEntity<>(new JwtDTO(token), token == null ? HttpStatus.UNAUTHORIZED : HttpStatus.OK);
    }

}
