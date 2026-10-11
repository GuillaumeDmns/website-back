package com.gdamiens.website.support;

import com.gdamiens.website.support.FakeServer.Response;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.gdamiens.website.support.MiniGtfs.METRO_1;
import static com.gdamiens.website.support.MiniGtfs.RER_A;

/**
 * What the external APIs answer by default in the integration tests, consistent with {@link MiniGtfs}
 */
public final class ExternalApis {

    /** Train of the RER A at Châtelet, then at Gare de Lyon (stop-monitoring of both) */
    public static final String RER_A_TRAIN = "154101";

    /** Daily quota of stop-monitoring, as PRIM says it */
    public static final int STOP_MONITORING_PER_DAY = 1_000_000;

    private ExternalApis() {
    }

    public static void stubDefaults(FakeServer server, GoogleTokens google) {
        server.on("/google/certs", Response.json(google.jwks()));

        server.on("/prim/stop-monitoring", request -> primCount(Response.json(SiriJson.stopMonitoring(visits(request.param("MonitoringRef")))),
            STOP_MONITORING_PER_DAY, server.requests("/prim/stop-monitoring").size()));
        server.on("/prim/estimated-timetable", request -> Response.json(SiriJson.estimatedTimetable(
            ("STIF:Line::" + RER_A + ":").equals(request.param("LineRef")) ? rerAJourneys() : List.of())));

        server.on("/prim/v2/navitia/journeys", request -> Response.json(Fixtures.read(
            "only".equals(request.param("direct_path")) ? "navitia/walking-path.json" : "navitia/journeys.json")));
        server.on("/prim/v2/navitia/places", Response.json(Fixtures.read("navitia/places.json")));
        server.on("/prim/disruptions_bulk/disruptions/v2", Response.json(Fixtures.read("prim/disruptions_bulk.json")));
        server.on("/elevators", Response.json(Fixtures.read("opendata/etat-des-ascenseurs.json")));

        server.on("/velib/station_information.json", Response.json("""
            {"data": {"stations": [
              {"station_id": "1", "stationCode": "1001", "name": "Châtelet - Rivoli", "lat": 48.8590, "lon": 2.3480, "capacity": 30},
              {"station_id": "2", "stationCode": "12001", "name": "Gare de Lyon - Diderot", "lat": 48.8450, "lon": 2.3740, "capacity": 40},
              {"station_id": "3", "stationCode": "94001", "name": "Fontenay - Mairie", "lat": 48.8500, "lon": 2.4800, "capacity": 20}
            ]}}"""));
        server.on("/velib/station_status.json", Response.json("""
            {"data": {"stations": [
              {"station_id": "1", "is_installed": 1, "is_renting": 1, "is_returning": 1, "num_docks_available": 10, "last_reported": 1760000000,
               "num_bikes_available_types": [{"mechanical": 5}, {"ebike": 3}]},
              {"station_id": "2", "is_installed": 1, "is_renting": 1, "is_returning": 1, "num_docks_available": 12, "last_reported": 1760000000,
               "num_bikes_available_types": [{"mechanical": 0}, {"ebike": 1}]},
              {"station_id": "3", "is_installed": 0, "is_renting": 0, "is_returning": 0, "num_docks_available": 0, "last_reported": 1760000000,
               "num_bikes_available_types": []}
            ]}}"""));
    }

    /** [answer] with PRIM's count of the day's calls, as its gateway adds it */
    public static Response primCount(Response answer, int perDay, int used) {
        return answer.withHeader("x-ratelimit-limit-day", String.valueOf(perDay))
            .withHeader("x-ratelimit-remaining-day", String.valueOf(perDay - used));
    }

    /** Next vehicles at a stop area ({@code STIF:StopArea:SP:70001:}), relative to now */
    private static List<SiriJson.Visit> visits(String monitoringRef) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return switch (monitoringRef == null ? "" : monitoringRef) {
            case "STIF:StopArea:SP:70001:" -> List.of(
                new SiriJson.Visit(METRO_1, "Nation", in(now, 2), in(now, 3), null, null, null),
                new SiriJson.Visit(METRO_1, "Nation", in(now, 9), in(now, 9), null, null, null),
                // Ends its trip here: not a departure
                new SiriJson.Visit(METRO_1, "Châtelet", in(now, 1), in(now, 1), null, null, null),
                new SiriJson.Visit(RER_A, "Fontenay-sous-Bois", in(now, 5), in(now, 5), "A", "UPAC", RER_A_TRAIN));
            case "STIF:StopArea:SP:70002:" -> List.of(
                new SiriJson.Visit(RER_A, "Fontenay-sous-Bois", in(now, 7), in(now, 8), "A", "UPAC", RER_A_TRAIN));
            default -> List.of();
        };
    }

    /** One train of the RER A towards Fontenay, between Châtelet and Gare de Lyon */
    private static List<SiriJson.Journey> rerAJourneys() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return List.of(new SiriJson.Journey("RERA-1", RER_A, "Fontenay-sous-Bois", "IDFM:5002", "UPAC", List.of(
            new SiriJson.Call("IDFM:2002", in(now, 1), in(now, 1)),
            new SiriJson.Call("IDFM:3002", in(now, 4), in(now, 4)),
            new SiriJson.Call("IDFM:4002", in(now, 7), in(now, 7)),
            new SiriJson.Call("IDFM:5002", in(now, 10), in(now, 10)))));
    }

    private static Instant in(Instant now, int minutes) {
        return now.plus(Duration.ofMinutes(minutes));
    }
}
