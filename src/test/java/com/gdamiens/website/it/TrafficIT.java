package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/** Traffic info from Navitia traffic_reports */
class TrafficIT extends IntegrationTest {

    private Api user;

    @BeforeEach
    void signIn() {
        user = guest();
    }

    @Test
    void listsTheMainLinesAndTheDisruptedOnes() {
        JsonNode traffic = user.get("/api/v2/traffic").json();

        assertThat(traffic.values().stream().map(line -> line.path("line").path("name").asString()))
            // Metro, RER always; the bus only while disrupted; the night bus is fine
            .containsExactly("1", "A", "38");
        assertThat(traffic.path(0).path("severity").asString()).isEqualTo("BLOCKING");
        assertThat(traffic.path(0).path("titles").values().stream().map(JsonNode::asString)).containsExactly("Métro 1 : trafic interrompu");
        assertThat(traffic.path(2).path("severity").asString()).isEqualTo("DISRUPTED");
    }

    @Test
    void listsTheDisruptionsOfALineOnce() {
        JsonNode disruptions = user.get("/api/v2/lines/C01371/disruptions").json();

        // Navitia gives one disruption per impact: the same text twice
        assertThat(disruptions).singleElement().satisfies(disruption -> {
            assertThat(disruption.path("title").asString()).isEqualTo("Métro 1 : trafic interrompu");
            assertThat(disruption.path("message").asString()).isEqualTo("Trafic interrompu entre Châtelet et Nation.\n\nReprise estimée : 18h.");
        });
    }

    @Test
    void listsTheDisruptionsOfAStopArea() {
        JsonNode disruptions = user.get("/api/v2/stops/IDFM:70001/disruptions").json();

        assertThat(disruptions.values().stream().map(disruption -> disruption.path("category").asString()))
            .containsExactlyInAnyOrder("ELEVATOR", "TRAFFIC");
        assertThat(user.get("/api/v2/stops/IDFM:99999/disruptions").status()).isEqualTo(404);
    }
}
