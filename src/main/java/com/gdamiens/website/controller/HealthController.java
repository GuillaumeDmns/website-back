package com.gdamiens.website.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.gdamiens.website.configuration.AppVersion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public health check, for the deployment script and the uptime monitoring
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    private final AppVersion appVersion;

    public HealthController(JdbcTemplate jdbcTemplate, AppVersion appVersion) {
        this.jdbcTemplate = jdbcTemplate;
        this.appVersion = appVersion;
    }

    @GetMapping("/health")
    @Operation(summary = "200 when the application is up and its database answers, 503 otherwise; with the release running (\"0.6.0\", \"dev\")")
    public ResponseEntity<Map<String, String>> health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(Map.of("status", "UP", "version", appVersion.get()));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("status", "DOWN", "version", appVersion.get()));
        }
    }
}
