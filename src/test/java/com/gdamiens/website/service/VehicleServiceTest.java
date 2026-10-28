package com.gdamiens.website.service;

import com.gdamiens.website.client.PrimRealtimeClient;
import com.gdamiens.website.client.siri.EstimatedVehicleJourney;
import com.gdamiens.website.client.siri.IDFMResponse;
import com.gdamiens.website.controller.object.v2.LineBranch;
import com.gdamiens.website.controller.object.v2.LineDetail;
import com.gdamiens.website.controller.object.v2.LineDirection;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.StopRef;
import com.gdamiens.website.controller.object.v2.Vehicle;
import com.gdamiens.website.controller.object.v2.VehicleCall;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.repository.NetworkRepository.ScheduledCallRow;
import com.gdamiens.website.support.SiriJson;
import com.gdamiens.website.support.SiriJson.Call;
import com.gdamiens.website.support.SiriJson.Journey;
import com.gdamiens.website.utils.ParisTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Where vehicles are placed, on a Transilien-like line whose two branches (from W and from N) merge at M before
 * Paris, and a metro-like line followed stop by stop
 */
class VehicleServiceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final String TRAIN_LINE = "C09001";

    private static final String METRO_LINE = "C09002";

    // Stop areas of the train line, named after their quay number (quay IDFM:1 is in stop area IDFM:80001)
    private static final StopRef W1 = stop(1, "W1");
    private static final StopRef W2 = stop(2, "W2");
    private static final StopRef M = stop(3, "M");
    private static final StopRef A = stop(4, "A");
    private static final StopRef P = stop(5, "Paris");
    private static final StopRef N1 = stop(6, "N1");
    private static final StopRef N2 = stop(7, "N2");

    // The metro line
    private static final StopRef S1 = stop(11, "S1");
    private static final StopRef S2 = stop(12, "S2");
    private static final StopRef S3 = stop(13, "S3");
    private static final StopRef S4 = stop(14, "S4");
    private static final StopRef S5 = stop(15, "S5");

    private final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    private final LocalDate today = now.atZone(ParisTime.ZONE).toLocalDate();

    private final PrimRealtimeClient prim = mock(PrimRealtimeClient.class);

    private final NetworkService networkService = mock(NetworkService.class);

    private final NetworkRepository networkRepository = mock(NetworkRepository.class);

    private final TrafficService trafficService = mock(TrafficService.class);

    private final List<ScheduledCallRow> schedule = new ArrayList<>();

    private final VehicleService service = new VehicleService(prim, networkService, networkRepository, trafficService);

    @BeforeEach
    void network() {
        when(networkService.getLineDetail(TRAIN_LINE)).thenReturn(Optional.of(new LineDetail(line(TRAIN_LINE, TransportMode.TRANSILIEN), List.of(
            new LineDirection(0, List.of(branch(W1, W2, M, A, P), branch(N1, N2, M, A, P)))))));
        when(networkService.getLineDetail(METRO_LINE)).thenReturn(Optional.of(new LineDetail(line(METRO_LINE, TransportMode.METRO), List.of(
            new LineDirection(0, List.of(branch(S1, S2, S3, S4, S5))), new LineDirection(1, List.of(branch(S5, S4, S3, S2, S1)))))));
        when(networkRepository.findStopAreasOfQuays(any())).thenAnswer(invocation -> {
            Map<String, String> stopAreas = new HashMap<>();
            for (Object quayId : (Iterable<?>) invocation.getArgument(0)) {
                String id = quayId.toString();
                if (id.matches("IDFM:\\d+")) {
                    stopAreas.put(id, "IDFM:8" + "%04d".formatted(Integer.parseInt(id.substring("IDFM:".length()))));
                }
            }
            return stopAreas;
        });
        when(networkRepository.findScheduledCalls(eq("IDFM:" + TRAIN_LINE), any()))
            .thenAnswer(invocation -> today.equals(invocation.getArgument(1)) ? schedule : List.of());
        when(trafficService.getClosedStops(anyString())).thenReturn(Set.of());
    }

    @Test
    void aTrainComesFromTheBranchOfItsScheduledTrip() {
        // Two trains reach M at the same time, one from each branch: the train number tells this one comes from N
        trip("T-W", "123999", W1, -6, W2, -2, M, 2, A, 5, P, 9);
        trip("T-N", "123457", N1, -6, N2, -2, M, 2, A, 5, P, 9);
        feed(train("SNCF-1", "123457", call(M, 2), call(A, 5), call(P, 9)));

        Vehicle vehicle = single(service.getVehicles(TRAIN_LINE));

        assertThat(vehicle.branch()).isEqualTo(1);
        assertThat(vehicle.fromStopId()).isEqualTo(N2.id());
        assertThat(vehicle.toStopId()).isEqualTo(M.id());
        // Two minutes left of the four from N2
        assertThat(vehicle.progress()).isCloseTo(0.5, within(0.02));
    }

    @Test
    void withoutScheduledTripTheTrainTakesTheStopBeforeOnTheFirstBranch() {
        feed(train("SNCF-1", "123457", call(M, 2), call(A, 5), call(P, 9)));

        Vehicle vehicle = single(service.getVehicles(TRAIN_LINE));

        assertThat(vehicle.branch()).isZero();
        assertThat(vehicle.fromStopId()).isEqualTo(W2.id());
    }

    @Test
    void aDirectTrainComesFromTheLastStopItServed() {
        // Known by its mission code only: its scheduled times (aimed) match a trip skipping W2 and A; a minute late
        trip("T-OMNIBUS", null, W1, 1, W2, 5, M, 9, A, 12, P, 16);
        trip("T-DIRECT", null, W1, -5, M, 3, P, 11);
        feed(train("SNCF-2", "SEPT", call(M, 3, 4), call(P, 11, 12)));

        Vehicle vehicle = single(service.getVehicles(TRAIN_LINE));

        assertThat(vehicle.branch()).isZero();
        assertThat(vehicle.fromStopId()).isEqualTo(W1.id());
        assertThat(vehicle.toStopId()).isEqualTo(M.id());
        // Four minutes left of its eight-minute ride from W1, delay included
        assertThat(vehicle.progress()).isCloseTo(0.5, within(0.02));
        assertThat(vehicle.delaySeconds()).isEqualTo(60);
        assertThat(vehicle.calls()).extracting(VehicleCall::stopName).containsExactly("M", "Paris");
    }

    @Test
    void theCallsAlreadyMadeWinOverTheSchedule() {
        // The schedule says N2, the real time says it left N1 three minutes ago
        trip("T-N", "123457", N1, -6, N2, -2, M, 2, A, 5, P, 9);
        feed(train("SNCF-1", "123457", call(M, 2), call(A, 5), call(P, 9)), journey -> {
            ObjectNode recorded = journey.putObject("RecordedCalls").putArray("RecordedCall").addObject();
            recorded.putObject("StopPointRef").put("value", "STIF:StopPoint:Q:6:");
            recorded.put("Order", 1);
            recorded.put("AimedDepartureTime", at(-6).toString());
            recorded.put("ActualDepartureTime", at(-3).toString());
        });

        Vehicle vehicle = single(service.getVehicles(TRAIN_LINE));

        assertThat(vehicle.branch()).isEqualTo(1);
        assertThat(vehicle.fromStopId()).isEqualTo(N1.id());
        assertThat(vehicle.progress()).isCloseTo(0.6, within(0.02));
    }

    @Test
    void aClosedStopIsNotWhereATrainComesFrom() {
        trip("T-N", "123457", N1, -6, N2, -2, M, 2, A, 5, P, 9);
        when(trafficService.getClosedStops(TRAIN_LINE)).thenReturn(Set.of(N2.id()));
        feed(train("SNCF-1", "123457", call(M, 2), call(A, 5), call(P, 9)));

        Vehicle vehicle = single(service.getVehicles(TRAIN_LINE));

        assertThat(vehicle.fromStopId()).isEqualTo(N1.id());
        assertThat(vehicle.progress()).isCloseTo(0.75, within(0.02));
    }

    @Test
    void aTrainStartingMidBranchWaitsThere() {
        trip("T-M", "124000", M, 2, A, 5, P, 9);
        feed(train("SNCF-3", "124000", call(M, 2), call(A, 5), call(P, 9)));

        Vehicle vehicle = single(service.getVehicles(TRAIN_LINE));

        assertThat(vehicle.fromStopId()).isNull();
        assertThat(vehicle.toStopId()).isEqualTo(M.id());
        assertThat(vehicle.progress()).isEqualTo(1);
    }

    @Test
    void aTrainStandingAtAStopIsThereUntilItsDeparture() {
        // Arrived at M a minute ago, leaves it in a minute: M stays its next stop, with its departure
        trip("T-N", "123457", N1, -6, N2, -4, M, -1, A, 5, P, 9);
        feed(train("SNCF-1", "123457", call(M, -1), call(A, 5), call(P, 9)), journey -> {
            ObjectNode atM = (ObjectNode) journey.path("EstimatedCalls").path("EstimatedCall").get(0);
            atM.put("ExpectedDepartureTime", at(1).toString());
        });

        Vehicle vehicle = single(service.getVehicles(TRAIN_LINE));

        assertThat(vehicle.toStopId()).isEqualTo(M.id());
        assertThat(vehicle.progress()).isEqualTo(1);
        assertThat(vehicle.calls().getFirst().departureAt()).isEqualTo(at(1));
        // Arrival and departure the same: no departure told
        assertThat(vehicle.calls().get(1).departureAt()).isNull();
    }

    @Test
    void aCancelledCallIsNotTheNextStop() {
        trip("T-N", "123457", N1, -6, N2, -2, M, 2, A, 5, P, 9);
        feed(train("SNCF-1", "123457", call(M, 2), call(A, 5), call(P, 9)), journey -> {
            ObjectNode first = (ObjectNode) journey.path("EstimatedCalls").path("EstimatedCall").get(0);
            first.put("ArrivalStatus", "cancelled");
            first.put("DepartureStatus", "cancelled");
        });

        Vehicle vehicle = single(service.getVehicles(TRAIN_LINE));

        assertThat(vehicle.toStopId()).isEqualTo(A.id());
        assertThat(vehicle.calls()).extracting(VehicleCall::stopName).containsExactly("A", "Paris");
    }

    @Test
    void vehiclesFollowedStopByStopGoThroughClosedStops() {
        // S3 and S4 are closed: no passage there. One train reaches S2 in 1 min then S5 in 5; another waits at S1
        // for 4 min. A made-up journey mixing them (calls out of order on both directions).
        when(trafficService.getClosedStops(METRO_LINE)).thenReturn(Set.of(S3.id(), S4.id()));
        feed(METRO_LINE, new Journey("made-up-A", METRO_LINE, "S5", "IDFM:15", null, List.of(call(S2, 1), call(S1, 4), call(S5, 5))));

        List<Vehicle> vehicles = service.getVehicles(METRO_LINE);

        // Not a second train between S4 and S5
        assertThat(vehicles).singleElement().satisfies(vehicle -> {
            assertThat(vehicle.fromStopId()).isEqualTo(S1.id());
            assertThat(vehicle.toStopId()).isEqualTo(S2.id());
            assertThat(vehicle.calls()).extracting(VehicleCall::stopName).containsExactly("S2", "S5");
        });
    }

    @Test
    void aTrainMissingFromTheFeedIsKeptForTwoLoads() {
        trip("T-N", "123457", N1, -6, N2, -2, M, 2, A, 5, P, 9);
        feed(train("SNCF-1", "123457", call(M, 2), call(A, 5), call(P, 9)));
        Vehicle seen = single(service.getVehicles(TRAIN_LINE));
        assertThat(seen.stale()).isFalse();
        when(prim.getEstimatedVehicleJourneys(TRAIN_LINE)).thenReturn(List.of());

        for (int load = 1; load <= 2; load++) {
            Vehicle kept = single(reload(TRAIN_LINE));
            assertThat(kept.id()).isEqualTo(seen.id());
            assertThat(kept.stale()).isTrue();
            assertThat(kept.toStopId()).isEqualTo(M.id());
            assertThat(kept.progress()).isGreaterThanOrEqualTo(seen.progress());
        }
        assertThat(reload(TRAIN_LINE)).isEmpty();
    }

    @Test
    void aVehicleFollowedStopByStopKeepsItsId() {
        when(trafficService.getClosedStops(METRO_LINE)).thenReturn(Set.of(S3.id(), S4.id()));
        feed(METRO_LINE, new Journey("made-up-A", METRO_LINE, "S5", "IDFM:15", null, List.of(call(S2, 1), call(S1, 4), call(S5, 4))));
        Vehicle before = single(service.getVehicles(METRO_LINE));
        assertThat(before.toStopId()).isEqualTo(S2.id());

        // A minute later it has left S2: seen at S5 only, a new vehicle for the feed
        feed(METRO_LINE, new Journey("made-up-B", METRO_LINE, "S5", "IDFM:15", null, List.of(call(S5, 4), call(S1, 4), call(S2, 7))));
        Vehicle after = single(reload(METRO_LINE));

        assertThat(after.fromStopId()).isEqualTo(S2.id());
        assertThat(after.toStopId()).isEqualTo(S5.id());
        assertThat(after.id()).isEqualTo(before.id());
    }

    /** The next load of the line (cached a minute otherwise) */
    private List<Vehicle> reload(String lineId) {
        service.onGtfsImported();
        return service.getVehicles(lineId);
    }

    private static StopRef stop(int quay, String name) {
        return new StopRef("IDFM:8" + "%04d".formatted(quay), name, 48.8 + quay / 1000.0, 2.3);
    }

    private static String quayOf(StopRef stop) {
        return "IDFM:" + Integer.parseInt(stop.id().substring("IDFM:8".length()));
    }

    private static LineSummary line(String id, TransportMode mode) {
        return new LineSummary(id, id, id, mode, "000000", "FFFFFF");
    }

    private static LineBranch branch(StopRef... stops) {
        return new LineBranch(stops[stops.length - 1].name(), 10, List.of(stops), List.of());
    }

    private Instant at(int minutes) {
        return now.plus(Duration.ofMinutes(minutes));
    }

    /** A call on time, [minutes] from now */
    private Call call(StopRef stop, int minutes) {
        return call(stop, minutes, minutes);
    }

    private Call call(StopRef stop, int aimed, int expected) {
        return new Call(quayOf(stop), at(aimed), at(expected));
    }

    private Journey train(String id, String name, Call... calls) {
        return new Journey(id, TRAIN_LINE, "Paris", quayOf(P), name, List.of(calls));
    }

    /** A scheduled trip of the train line today: stops and their times in minutes from now */
    private void trip(String tripId, String number, Object... stopsAndMinutes) {
        Instant serviceDay = ParisTime.ofServiceDay(today, 0);
        for (int i = 0; i < stopsAndMinutes.length; i += 2) {
            int seconds = (int) Duration.between(serviceDay, at((Integer) stopsAndMinutes[i + 1])).toSeconds();
            schedule.add(new ScheduledCallRow(tripId, number, ((StopRef) stopsAndMinutes[i]).id(), seconds, seconds));
        }
    }

    private void feed(Journey journey) {
        feed(journey, node -> {
        });
    }

    /** The train line's estimated-timetable: [journey], changed by [change] */
    private void feed(Journey journey, Consumer<ObjectNode> change) {
        JsonNode root = JSON.readTree(SiriJson.estimatedTimetable(List.of(journey)));
        change.accept((ObjectNode) journeys(root).get(0));
        when(prim.getEstimatedVehicleJourneys(TRAIN_LINE)).thenReturn(parse(root));
    }

    private void feed(String lineId, Journey... journeys) {
        when(prim.getEstimatedVehicleJourneys(lineId)).thenReturn(parse(JSON.readTree(SiriJson.estimatedTimetable(List.of(journeys)))));
    }

    private static JsonNode journeys(JsonNode root) {
        return root.path("Siri").path("ServiceDelivery").path("EstimatedTimetableDelivery").get(0)
            .path("EstimatedJourneyVersionFrame").get(0).path("EstimatedVehicleJourney");
    }

    private static List<EstimatedVehicleJourney> parse(JsonNode root) {
        return PrimRealtimeClient.SIRI_JSON.readValue(root.toString(), IDFMResponse.class).getSiri().getServiceDelivery()
            .getEstimatedTimetableDelivery().getFirst().getEstimatedJourneyVersionFrame().getFirst().getEstimatedVehicleJourney();
    }

    private static Vehicle single(List<Vehicle> vehicles) {
        assertThat(vehicles).hasSize(1);
        return vehicles.getFirst();
    }
}
