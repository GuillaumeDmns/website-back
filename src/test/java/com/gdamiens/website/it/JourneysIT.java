package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.FakeServer.Request;
import com.gdamiens.website.support.FakeServer.Response;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/** Journeys planned by Navitia, with the GTFS lines, paths and the usage budgets */
class JourneysIT extends IntegrationTest {

    /** Searches within a minute are shared (positions within ~50 m): each test starts from its own place */
    private static final AtomicInteger PLACES = new AtomicInteger();

    @Test
    void plansAJourney() {
        Api user = newUser().api();

        JsonNode plan = user.get("/api/v2/journeys?from=48.8512,2.3401&to=IDFM:70002").json();

        assertThat(plan.path("journeys")).hasSize(3);
        JsonNode best = plan.path("journeys").path(0);
        assertThat(best.path("sections").values().stream().map(section -> section.path("kind").asString())).containsExactly("WALK", "TRANSIT");
        assertThat(best.path("sections").path(1).path("line").path("id").asString()).isEqualTo("C01371");
        assertThat(best.path("sections").path(1).path("line").path("longName").asString()).isEqualTo("Métro 1");
        assertThat(plan.path("earlier").path("arriveBy").asBoolean()).isTrue();
        assertThat(plan.path("later").path("arriveBy").asBoolean()).isFalse();

        Request request = navitiaJourneyRequests().getFirst();
        // Positions go to Navitia on a grid of 0.0005°, shared with the searches next to them
        assertThat(request.param("from")).isEqualTo("2.3400;48.8510");
        assertThat(request.param("to")).isEqualTo("stop_area:IDFM:70002");
        assertThat(request.param("data_freshness")).isEqualTo("realtime");
        assertThat(request.param("datetime_represents")).isEqualTo("departure");
        assertThat(request.params("forbidden_uris[]")).isEmpty();
    }

    @Test
    void passesTheOptions() {
        Api user = newUser().api();

        user.get(journey() + "&datetime=2026-10-10T08:30:00Z&arriveBy=true&modes=METRO,RER&wheelchair=true&walkingSpeed=SLOW&maxTransfers=1");

        Request request = navitiaJourneyRequests().getFirst();
        assertThat(request.param("datetime")).isEqualTo("20261010T103000");
        assertThat(request.param("datetime_represents")).isEqualTo("arrival");
        assertThat(request.param("wheelchair")).isEqualTo("true");
        assertThat(request.param("walking_speed")).isEqualTo("0.9");
        assertThat(request.param("max_nb_transfers")).isEqualTo("1");
        assertThat(request.params("forbidden_uris[]")).contains("physical_mode:Bus").doesNotContain("physical_mode:Metro");
    }

    @Test
    void drawsAStraightRideAlongItsGtfsPath() throws InterruptedException {
        Api user = newUser().api();
        String journey = journey();

        // Navitia only gives the two stops of the metro ride: the GTFS shape of the line replaces them (looked up within
        // 300 ms, then cached for the next requests)
        int points = 0;
        for (int attempt = 0; attempt < 10 && points <= 2; attempt++) {
            points = user.get(journey).json().path("journeys").path(0).path("sections").path(1).path("shape").size();
            Thread.sleep(200);
        }
        assertThat(points).isGreaterThan(2);
    }

    @Test
    void noSolutionIsAnEmptyPlan() {
        EXTERNAL.on("/prim/v2/navitia/journeys", Response.json(404, "{\"error\":{\"id\":\"no_solution\",\"message\":\"no solution found\"}}"));

        Api.Response plan = newUser().api().get(journey());

        assertThat(plan.status()).isEqualTo(200);
        assertThat(plan.json().path("journeys")).isEmpty();
    }

    @Test
    void refusesUnknownPlaces() {
        Api user = newUser().api();

        assertThat(user.get("/api/v2/journeys?from=Chatelet&to=IDFM:70002").status()).isEqualTo(400);
        EXTERNAL.on("/prim/v2/navitia/journeys", Response.json(400, "{\"error\":{\"id\":\"unknown_object\"}}"));
        assertThat(user.get(journey()).status()).isEqualTo(400);
    }

    @Test
    void guestsHaveAFewJourneysADay() {
        Api guest = guest();
        // The same search answered from the cache counts too
        String journey = journey();
        for (int i = 0; i < 10; i++) {
            assertThat(guest.get(journey).status()).isEqualTo(200);
        }

        Api.Response limited = guest.get(journey());

        assertThat(limited.status()).isEqualTo(429);
        assertThat(limited.json().path("code").asString()).isEqualTo("guest_journey_limit");
    }

    @Test
    void stopsCallingNavitiaWhileItIsDown() {
        EXTERNAL.on("/prim/v2/navitia/journeys", Response.status(503));
        Api user = newUser().api();
        // Failures are not cached: each search asks Navitia
        String journey = journey();
        for (int i = 0; i < 5; i++) {
            assertThat(user.get(journey).status()).isEqualTo(502);
        }

        Api.Response refused = user.get(journey);

        assertThat(refused.status()).isEqualTo(503);
        assertThat(refused.json().path("code").asString()).isEqualTo("prim_unavailable");
        assertThat(navitiaJourneyRequests()).hasSize(5);
    }

    @Test
    void sharesTheSameSearchWithinAMinute() {
        String datetime = "&to=IDFM:70002&datetime=2026-10-10T08:30:";

        // Two travellers side by side, a few seconds apart
        JsonNode first = newUser().api().get("/api/v2/journeys?from=48.8301,2.3301" + datetime + "00Z").json();
        JsonNode second = newUser().api().get("/api/v2/journeys?from=48.8302,2.3302" + datetime + "40Z").json();

        assertThat(second).isEqualTo(first);
        assertThat(navitiaJourneyRequests()).hasSize(1);

        // Another minute is another search
        newUser().api().get("/api/v2/journeys?from=48.8301,2.3301&to=IDFM:70002&datetime=2026-10-10T08:31:00Z");
        assertThat(navitiaJourneyRequests()).hasSize(2);
    }

    @Test
    void addsAVelibOption() {
        Api user = newUser().api();

        JsonNode journeys = user.get("/api/v2/journeys?from=48.8584,2.3470&to=48.8443,2.3730&bikeShare=true").json().path("journeys");

        JsonNode bike = journeys.values().stream().filter(journey -> "bike_share".equals(journey.path("type").asString())).findFirst().orElseThrow();
        assertThat(bike.path("sections").values().stream().map(section -> section.path("kind").asString())).containsExactly("WALK", "BIKE", "WALK");
        assertThat(bike.path("sections").path(1).path("from").path("name").asString()).isEqualTo("Station Vélib Châtelet - Rivoli");
        assertThat(bike.path("sections").path(1).path("to").path("name").asString()).isEqualTo("Station Vélib Gare de Lyon - Diderot");
        // The ride follows Navitia's walking path between the stations
        assertThat(bike.path("sections").path(1).path("length").asInt()).isEqualTo(2600);
    }

    /** A search no other test made */
    private static String journey() {
        return String.format(Locale.ROOT, "/api/v2/journeys?from=%.4f,2.3200&to=IDFM:70002", 48.80 + PLACES.incrementAndGet() * 0.001);
    }

    private static List<Request> navitiaJourneyRequests() {
        return EXTERNAL.requests("/prim/v2/navitia/journeys").stream().filter(request -> request.param("direct_path") == null).toList();
    }
}
