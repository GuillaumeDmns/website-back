package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.FakeServer.Response;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Network data read from the imported GTFS: stops, lines, branches, timetables, search, Vélib */
class NetworkIT extends IntegrationTest {

    private Api user;

    @BeforeEach
    void signIn() {
        user = guest();
    }

    @Test
    void findsStopAreasAroundAPosition() {
        JsonNode nearby = user.get("/api/v2/nearby?lat=48.8584&lon=2.3470&radius=500").json();

        assertThat(names(nearby)).containsExactly("Châtelet", "Les Halles");
        assertThat(nearby.path(0).path("distance").asInt()).isZero();
        assertThat(nearby.path(0).path("lines").values().stream().map(line -> line.path("name").asString()))
            .containsExactly("1", "A", "38", "N11");
        assertThat(user.get("/api/v2/nearby?lat=48.8584&lon=2.3470&radius=5000&limit=3").json()).hasSize(3);
    }

    @Test
    void refusesInvalidPositions() {
        assertThat(user.get("/api/v2/nearby?lat=100&lon=2.3").status()).isEqualTo(400);
        Api.Response notANumber = user.get("/api/v2/nearby?lat=abc&lon=2.3");
        assertThat(notANumber.status()).isEqualTo(400);
        assertThat(notANumber.header("Content-Type")).startsWith("application/problem+json");
        assertThat(user.get("/api/v2/nearby?lat=48.85").status()).isEqualTo(400);
    }

    @Test
    void describesAStopArea() {
        JsonNode stop = user.get("/api/v2/stops/IDFM:70001").json();

        assertThat(stop.path("name").asString()).isEqualTo("Châtelet");
        assertThat(stop.path("quays")).hasSize(3);
        assertThat(stop.path("quays").values().stream().filter(quay -> "A".equals(quay.path("platformCode").asString(null))))
            .singleElement().satisfies(quay -> assertThat(quay.path("lineIds").values().stream().map(JsonNode::asString))
                .containsExactly("C01742"));
        // Its quays don't all have step-free boarding
        assertThat(stop.path("wheelchairBoarding").asInt()).isZero();
        assertThat(stop.path("connections")).singleElement().satisfies(connection -> {
            assertThat(connection.path("id").asString()).isEqualTo("IDFM:70007");
            assertThat(connection.path("minTransferSeconds").asInt()).isEqualTo(240);
        });
    }

    @Test
    void answersNotFoundForUnknownIds() {
        Api.Response stop = user.get("/api/v2/stops/IDFM:99999");
        assertThat(stop.status()).isEqualTo(404);
        assertThat(stop.json().path("detail").asString()).isEqualTo("Stop area IDFM:99999 not found");

        assertThat(user.get("/api/v2/lines/C99999").status()).isEqualTo(404);
        assertThat(user.get("/api/v2/lines/C99999/vehicles").status()).isEqualTo(404);
        assertThat(user.get("/api/v2/stops/IDFM:99999/timetable?lineId=C01371").status()).isEqualTo(404);
    }

    @Test
    void groupsTheRerAIntoDirectionsAndBranches() {
        JsonNode line = user.get("/api/v2/lines/C01742").json();

        assertThat(line.path("line").path("mode").asString()).isEqualTo("RER");
        JsonNode directions = line.path("directions");
        assertThat(directions).hasSize(2);

        // Eastbound: the express trains merged into the Fontenay branch, then the Joinville one
        JsonNode east = directions.path(0).path("branches");
        assertThat(east).hasSize(2);
        assertThat(stopNames(east.path(0))).containsExactly("Châtelet", "Gare de Lyon", "Nation", "Vincennes", "Fontenay-sous-Bois");
        assertThat(east.path(0).path("headsign").asString()).isEqualTo("Fontenay-sous-Bois");
        assertThat(east.path(0).path("tripCount").asInt()).isEqualTo(84 + 21);
        assertThat(stopNames(east.path(1))).containsExactly("Châtelet", "Gare de Lyon", "Nation", "Vincennes", "Joinville-le-Pont");
        assertThat(east.path(0).path("shape").size()).isGreaterThan(2);

        // Westbound trains, also in GTFS direction 0, are oriented the other way
        assertThat(directions.path(1).path("directionId").asInt()).isEqualTo(1);
        assertThat(stopNames(directions.path(1).path("branches").path(0)))
            .containsExactly("Fontenay-sous-Bois", "Vincennes", "Nation", "Gare de Lyon", "Châtelet");
    }

    @Test
    void listsTheDayTimetableOfALine() {
        JsonNode timetable = user.get("/api/v2/stops/IDFM:70001/timetable?lineId=C01742&date=2026-10-12").json();

        assertThat(timetable.path("date").asString()).isEqualTo("2026-10-12");
        // Trains from Châtelet only go east: those ending there are arrivals
        assertThat(timetable.path("directions")).singleElement().satisfies(direction -> {
            assertThat(direction.path("name").asString()).isEqualTo("Fontenay-sous-Bois / Joinville-le-Pont");
            assertThat(direction.path("departures")).hasSize(84 + 42 + 21);
            assertThat(Instant.parse(direction.path("departures").path(0).path("time").asString()))
                .isEqualTo(Instant.parse("2026-10-12T03:00:00Z"));
            // The last ones leave after midnight, on the same service day
            assertThat(Instant.parse(direction.path("departures").path(84 + 42 + 20).path("time").asString()))
                .isAfter(Instant.parse("2026-10-12T22:00:00Z"));
        });
    }

    @Test
    void searchesLinesAndPlaces() {
        JsonNode result = user.get("/api/v2/search?q=Chatelet").json();

        assertThat(result.path("lines")).isEmpty();
        JsonNode places = result.path("places");
        assertThat(places.values().stream().map(place -> place.path("type").asString())).containsExactly("STOP_AREA", "ADDRESS", "POI");
        assertThat(places.path(0).path("id").asString()).isEqualTo("IDFM:70001");
        assertThat(places.path(0).path("lines")).hasSize(4);
        assertThat(places.path(1).path("lat").asDouble()).isEqualTo(48.8598);
        assertThat(EXTERNAL.requests("/prim/v2/navitia/places")).singleElement().satisfies(request -> {
            assertThat(request.param("q")).isEqualTo("chatelet");
            assertThat(request.params("type[]")).containsExactly("stop_area", "address", "poi");
            assertThat(request.header("apiKey")).isEqualTo("test-prim-key");
        });

        assertThat(user.get("/api/v2/search?q=rer%20a").json().path("lines").values().stream().map(line -> line.path("id").asString()))
            .containsExactly("C01742");
        assertThat(user.get("/api/v2/search?q=%20%20").json().path("places")).isEmpty();
    }

    @Test
    void searchesLinesWhenNavitiaIsDown() {
        EXTERNAL.on("/prim/v2/navitia/places", Response.status(503));

        JsonNode result = user.get("/api/v2/search?q=n11").json();

        assertThat(result.path("lines").values().stream().map(line -> line.path("mode").asString())).containsExactly("NOCTILIEN");
        assertThat(result.path("places")).isEmpty();
    }

    @Test
    void listsVelibStations() {
        JsonNode nearby = user.get("/api/v2/bikes/nearby?lat=48.8584&lon=2.3470&radius=2000").json();
        assertThat(nearby).singleElement().satisfies(station -> {
            assertThat(station.path("name").asString()).isEqualTo("Châtelet - Rivoli");
            assertThat(station.path("mechanical").asInt()).isEqualTo(5);
            assertThat(station.path("electric").asInt()).isEqualTo(3);
            assertThat(station.path("distance").asInt()).isBetween(50, 150);
        });

        JsonNode inBox = user.get("/api/v2/bikes/stations?minLat=48.80&minLon=2.30&maxLat=48.90&maxLon=2.50").json();
        // The station not installed is left out
        assertThat(names(inBox)).containsExactlyInAnyOrder("Châtelet - Rivoli", "Gare de Lyon - Diderot");
    }

    private static List<String> names(JsonNode array) {
        return array.values().stream().map(item -> item.path("name").asString()).toList();
    }

    private static List<String> stopNames(JsonNode branch) {
        return names(branch.path("stops"));
    }
}
