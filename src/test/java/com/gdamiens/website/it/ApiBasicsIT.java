package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Health, CORS, rate limits and error format, common to every endpoint */
class ApiBasicsIT extends IntegrationTest {

    @Test
    void isHealthy() {
        Api.Response health = api.get("/api/health");

        assertThat(health.status()).isEqualTo(200);
        assertThat(health.json().path("status").asString()).isEqualTo("UP");
        // A build without -Dapp.version (not from a release tag)
        assertThat(health.json().path("version").asString()).isEqualTo("dev");
    }

    @Test
    void allowsTheConfiguredOriginsOnly() {
        Api.Response allowed = api.preflight("/api/v2/traffic", "https://app.example.com", "GET");
        assertThat(allowed.status()).isEqualTo(200);
        assertThat(allowed.header("Access-Control-Allow-Origin")).isEqualTo("https://app.example.com");

        assertThat(api.preflight("/api/v2/traffic", "https://evil.example.com", "GET").status()).isEqualTo(403);
    }

    @Test
    void limitsAnonymousRequestsPerAddress() {
        Api client = api.from("198.19.1.1");
        for (int i = 0; i < 60; i++) {
            assertThat(client.get("/api/health").status()).isEqualTo(200);
        }

        Api.Response limited = client.get("/api/health");

        assertThat(limited.status()).isEqualTo(429);
        assertThat(limited.header("Retry-After")).isNotNull();
        assertThat(limited.header("Content-Type")).startsWith("application/problem+json");
        assertThat(api.get("/api/health").status()).isEqualTo(200);
    }

    @Test
    void answersErrorsAsProblemDetails() {
        Api.Response missing = guest().get("/api/v2/stops/IDFM:70001/timetable");

        assertThat(missing.status()).isEqualTo(400);
        assertThat(missing.header("Content-Type")).startsWith("application/problem+json");
        assertThat(missing.json().path("status").asInt()).isEqualTo(400);
    }
}
