package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.Credentials;
import com.gdamiens.website.controller.object.JwtDTO;
import com.gdamiens.website.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @ApiOperation(value = "Get the JWT")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")})
    public ResponseEntity<JwtDTO> login(@RequestBody Credentials credentials) {

        String token = userService.signIn(credentials.getUsername(), credentials.getPassword());
        return new ResponseEntity<>(new JwtDTO(token), token == null ? HttpStatus.UNAUTHORIZED : HttpStatus.OK);
    }

    @GetMapping("/refresh")
    @ApiOperation(value = "Refresh the JWT", authorizations = {@Authorization(value = "Auth. Token")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<JwtDTO> refresh(HttpServletRequest req) {
        String token = userService.refresh(req.getRemoteUser());
        return new ResponseEntity<>(new JwtDTO(token), token == null ? HttpStatus.UNAUTHORIZED : HttpStatus.OK);
    }

}
