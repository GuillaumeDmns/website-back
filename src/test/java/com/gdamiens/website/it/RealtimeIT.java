package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.ExternalApis;
import com.gdamiens.website.support.FakeServer.Response;
import com.gdamiens.website.support.IntegrationTest;
import com.gdamiens.website.support.SiriJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** Departures, vehicles and rides from PRIM real time merged with the GTFS */
class RealtimeIT extends IntegrationTest {

    private Api user;

    @BeforeEach
    void signIn() {
        user = newUser().api();
    }

    @Test
    void mergesRealTimeAndScheduledDepartures() {
        JsonNode departures = user.get("/api/v2/stops/IDFM:70001/departures?limit=5").json();

        assertThat(departures.path("realtimeAvailable").asBoolean()).isTrue();
        JsonNode lines = departures.path("lines");
        assertThat(lines.values().stream().map(group -> group.path("line").path("name").asString() + " → " + group.path("destination").asString()))
            .startsWith("1 → Nation", "A → Fontenay-sous-Bois")
            // The metro ending its trip here is not a departure; the bus has no real time: from the schedule
            .doesNotContain("1 → Châtelet")
            .contains("38 → Gare de Lyon");

        JsonNode metro = lines.path(0).path("departures");
        assertThat(metro).hasSize(2);
        assertThat(metro.path(0).path("realtime").asBoolean()).isTrue();
        assertThat(Duration.between(Instant.parse(metro.path(0).path("aimedTime").asString()), Instant.parse(metro.path(0).path("time").asString())))
            .isEqualTo(Duration.ofMinutes(1));

        JsonNode rer = lines.path(1).path("departures").path(0);
        assertThat(rer.path("mission").asString()).isEqualTo("UPAC");
        assertThat(rer.path("trainNumber").asString()).isEqualTo(ExternalApis.RER_A_TRAIN);
        assertThat(rer.path("platform").asString()).isEqualTo("A");

        JsonNode bus = lines.values().stream().filter(group -> group.path("line").path("name").asString().equals("38")).findFirst().orElseThrow();
        assertThat(bus.path("departures").path(0).path("realtime").asBoolean()).isFalse();

        assertThat(EXTERNAL.requests("/prim/stop-monitoring")).allSatisfy(request -> assertThat(request.header("apiKey")).isEqualTo("test-prim-key"));
    }

    @Test
    void filtersALine() {
        JsonNode lines = user.get("/api/v2/stops/IDFM:70001/departures?lineId=C01742&limit=1").json().path("lines");

        assertThat(lines).singleElement().satisfies(group -> assertThat(group.path("departures")).hasSize(1));
    }

    @Test
    void fallsBackToTheScheduleWhenPrimFails() {
        EXTERNAL.on("/prim/stop-monitoring", request -> "STIF:StopArea:SP:70003:".equals(request.param("MonitoringRef"))
            ? Response.status(503)
            : Response.json(SiriJson.stopMonitoring(List.of())));

        Api.Response departures = user.get("/api/v2/stops/IDFM:70003/departures");

        assertThat(departures.status()).isEqualTo(200);
        assertThat(departures.json().path("realtimeAvailable").asBoolean()).isFalse();
        assertThat(departures.json().path("lines").values().stream()
            .flatMap(group -> group.path("departures").values().stream())
            .noneMatch(departure -> departure.path("realtime").asBoolean())).isTrue();
        assertThat(EXTERNAL.requests("/prim/stop-monitoring")).hasSize(1);
    }

    @Test
    void listsTheDeparturesAroundAPosition() {
        JsonNode nearby = user.get("/api/v2/nearby/departures?lat=48.8584&lon=2.3470&radius=300&maxStops=3").json();

        assertThat(nearby).singleElement().satisfies(stop -> {
            assertThat(stop.path("stop").path("id").asString()).isEqualTo("IDFM:70001");
            assertThat(stop.path("stop").path("distance").asInt()).isZero();
            assertThat(stop.path("realtimeAvailable").asBoolean()).isTrue();
        });
    }

    @Test
    void placesTheVehiclesOfALine() {
        JsonNode vehicles = user.get("/api/v2/lines/C01742/vehicles").json();

        assertThat(vehicles).singleElement().satisfies(vehicle -> {
            assertThat(vehicle.path("id").asString()).isEqualTo("RERA-1");
            assertThat(vehicle.path("name").asString()).isEqualTo("UPAC");
            assertThat(vehicle.path("destination").asString()).isEqualTo("Fontenay-sous-Bois");
            assertThat(vehicle.path("direction").asInt()).isZero();
            assertThat(vehicle.path("fromStopId").asString()).isEqualTo("IDFM:70001");
            assertThat(vehicle.path("toStopId").asString()).isEqualTo("IDFM:70002");
            assertThat(vehicle.path("progress").asDouble()).isBetween(0.0, 1.0);
            assertThat(vehicle.path("calls").values().stream().map(call -> call.path("stopName").asString()))
                .containsExactly("Gare de Lyon", "Nation", "Vincennes", "Fontenay-sous-Bois");
        });
        assertThat(EXTERNAL.requests("/prim/estimated-timetable")).singleElement()
            .satisfies(request -> assertThat(request.param("LineRef")).isEqualTo("STIF:Line::C01742:"));

        // Cached a minute: the estimated-timetable quota is small
        user.get("/api/v2/lines/C01742/vehicles");
        assertThat(EXTERNAL.requests("/prim/estimated-timetable")).hasSize(1);
    }

    @Test
    void followsVehiclesStopByStopWhenJourneysMixThem() {
        // RATP lines only give the next passages per stop, under made-up journeys mixing vehicles: one train is at
        // Châtelet (its first stop) leaving in 2 min, then Gare de Lyon +4, Nation +6; another left Châtelet and
        // reaches Gare de Lyon in 1 min, Nation in 3
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        EXTERNAL.on("/prim/estimated-timetable", Response.json(SiriJson.estimatedTimetable(List.of(
            new SiriJson.Journey("made-up-A", "C01371", "Nation", "IDFM:3001", null, List.of(
                new SiriJson.Call("IDFM:3001", now.plus(Duration.ofMinutes(3)), now.plus(Duration.ofMinutes(3))),
                new SiriJson.Call("IDFM:2001", now.plus(Duration.ofMinutes(4)), now.plus(Duration.ofMinutes(4))),
                new SiriJson.Call("IDFM:1001", now.plus(Duration.ofMinutes(2)), now.plus(Duration.ofMinutes(2))))),
            new SiriJson.Journey("made-up-B", "C01371", "Nation", "IDFM:3001", null, List.of(
                new SiriJson.Call("IDFM:2001", now.plus(Duration.ofMinutes(1)), now.plus(Duration.ofMinutes(1))),
                new SiriJson.Call("IDFM:3001", now.plus(Duration.ofMinutes(6)), now.plus(Duration.ofMinutes(6)))))))));

        JsonNode vehicles = user.get("/api/v2/lines/C01371/vehicles").json();

        assertThat(vehicles).hasSize(2);
        assertThat(vehicles.values().stream().map(vehicle -> vehicle.path("id").asString())).noneMatch(id -> id.startsWith("made-up"));
        JsonNode waiting = vehicles.values().stream().filter(vehicle -> vehicle.path("fromStopId").isNull()).findFirst().orElseThrow();
        assertThat(waiting.path("toStopId").asString()).isEqualTo("IDFM:70001");
        assertThat(waiting.path("calls").values().stream().map(call -> call.path("stopName").asString()))
            .containsExactly("Châtelet", "Gare de Lyon", "Nation");
        JsonNode running = vehicles.values().stream().filter(vehicle -> !vehicle.path("fromStopId").isNull()).findFirst().orElseThrow();
        assertThat(running.path("fromStopId").asString()).isEqualTo("IDFM:70001");
        assertThat(running.path("toStopId").asString()).isEqualTo("IDFM:70002");
        assertThat(running.path("destination").asString()).isEqualTo("Nation");
        // Halfway: a minute left on a two-minute segment
        assertThat(running.path("progress").asDouble()).isCloseTo(0.5, within(0.05));
        assertThat(running.path("calls").values().stream().map(call -> call.path("stopName").asString()))
            .containsExactly("Gare de Lyon", "Nation");
    }

    @Test
    void aLineWithoutRealTimeHasNoVehicles() {
        Api.Response vehicles = user.get("/api/v2/lines/C02000/vehicles");

        assertThat(vehicles.status()).isEqualTo(200);
        assertThat(vehicles.json()).isEmpty();
    }

    @Test
    void followsARideToItsArrival() {
        JsonNode rides = user.get("/api/v2/lines/C01742/rides?from=IDFM:70001&to=IDFM:70002").json();

        JsonNode first = rides.path(0);
        assertThat(first.path("departure").path("trainNumber").asString()).isEqualTo(ExternalApis.RER_A_TRAIN);
        assertThat(first.path("destination").asString()).isEqualTo("Fontenay-sous-Bois");
        // Same train in the stop-monitoring of Gare de Lyon
        assertThat(first.path("arrivalSource").asString()).isEqualTo("realtime");
        assertThat(Duration.between(Instant.parse(first.path("departure").path("time").asString()), Instant.parse(first.path("arrivalAt").asString())))
            .isEqualTo(Duration.ofMinutes(3));
        // Then trains from the schedule
        assertThat(rides.values().stream().skip(1)).allSatisfy(ride -> assertThat(ride.path("arrivalSource").asString()).isEqualTo("scheduled"));
    }

    @Test
    void refusesRidesTooFarAhead() {
        Api.Response rides = user.get("/api/v2/lines/C01742/rides?from=IDFM:70001&to=IDFM:70002&after=" + Instant.now().plus(Duration.ofDays(1)));

        assertThat(rides.status()).isEqualTo(400);
    }
}
