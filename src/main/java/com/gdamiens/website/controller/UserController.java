package com.gdamiens.website.controller;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.Credentials;
import com.gdamiens.website.controller.object.JwtDTO;
import com.gdamiens.website.idfm.IDFMResponse;
import com.gdamiens.website.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private static final String IDFM_URL = "https://prim.iledefrance-mobilites.fr/marketplace/estimated-timetable?LineRef=ALL";

    private final UserService userService;

    private final HttpComponentsClientHttpRequestFactory requestFactory;

    private final ApplicationProperties applicationProperties;

    public UserController(UserService userService, ApplicationProperties applicationProperties) {
        this.userService = userService;
        this.requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build());
        this.applicationProperties = applicationProperties;
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

    @GetMapping("/swag")
    @Operation(summary = "Swag", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<String> swag() {

        try {
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Accept-encoding", "gzip, deflate");
            headers.set("apiKey", applicationProperties.getIdfmKey());

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<IDFMResponse> response = new RestTemplate(this.requestFactory).exchange(IDFM_URL, HttpMethod.GET, request, IDFMResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                // TODO process entity
                log.info("success requesting IDFM");
            }
        } catch (Exception e) {
            log.info("error during IDFM request");
        }

        return new ResponseEntity<>("ok", HttpStatus.OK);
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
