package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/** Traffic info from PRIM disruptions_bulk, elevators from the open data */
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

        // IDFM sometimes gives the same text twice
        assertThat(disruptions).singleElement().satisfies(disruption -> {
            assertThat(disruption.path("title").asString()).isEqualTo("Métro 1 : trafic interrompu");
            assertThat(disruption.path("message").asString()).isEqualTo("Trafic interrompu entre Châtelet et Nation.\n\nReprise estimée : 18h.");
            // Stop names of the GTFS
            JsonNode section = disruption.path("sections").path(0);
            assertThat(section.path("lineId").asString()).isEqualTo("C01371");
            assertThat(section.path("fromName").asString()).isEqualTo("Châtelet");
            assertThat(section.path("toId").asString()).isEqualTo("IDFM:70003");
            assertThat(section.path("toName").asString()).isEqualTo("Nation");
        });
    }

    @Test
    void listsTheDisruptionsOfAStopArea() {
        JsonNode disruptions = user.get("/api/v2/stops/IDFM:70001/disruptions").json();

        // Its elevator, its closed bus stop, the metro 1 stopped
        assertThat(disruptions.values().stream().map(disruption -> disruption.path("title").asString())).containsExactlyInAnyOrder(
            "Ascenseur hors service : Ligne M1 direction Nation", "Bus 38 : arrêt Châtelet non desservi", "Métro 1 : trafic interrompu");
        assertThat(disruptions.values().stream().filter(disruption -> "ELEVATOR".equals(disruption.path("category").asString())))
            .hasSize(1);
        assertThat(user.get("/api/v2/stops/IDFM:99999/disruptions").status()).isEqualTo(404);
    }

    @Test
    void sharesOneFetchBetweenEveryone() {
        user.get("/api/v2/traffic");
        guest().get("/api/v2/lines/C01742/disruptions");
        guest().get("/api/v2/stops/IDFM:70002/disruptions");

        // Fetched at most once for the whole test run (every 3 min), never from Navitia
        assertThat(EXTERNAL.requests("/prim/disruptions_bulk/disruptions/v2").size()).isLessThanOrEqualTo(1);
        assertThat(EXTERNAL.requests("/elevators").size()).isLessThanOrEqualTo(1);
        assertThat(EXTERNAL.requests("/prim/v2/navitia/traffic_reports")).isEmpty();
    }
}
