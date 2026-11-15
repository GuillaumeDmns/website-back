package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** What the apps ask at start-up (application-test.yml sets it) */
class AppConfigIT extends IntegrationTest {

    @Test
    void tellsTheAppsWithoutAToken() {
        Api.Response config = api.get("/api/v2/app-config");

        assertThat(config.status()).isEqualTo(200);
        assertThat(config.json().path("minVersions").path("android").asString()).isEqualTo("0.9.0");
        // A platform without a minimum supports every version
        assertThat(config.json().path("minVersions").has("web")).isFalse();
        assertThat(config.json().path("disabledFeatures").get(0).asString()).isEqualTo("vehicles");
        // Trimmed, and a language without text left out
        assertThat(config.json().path("notice").path("fr").asString()).isEqualTo("Grève mardi : trafic très perturbé");
        assertThat(config.json().path("notice").has("en")).isFalse();
        assertThat(config.header("Cache-Control")).contains("max-age=300");
    }
}
