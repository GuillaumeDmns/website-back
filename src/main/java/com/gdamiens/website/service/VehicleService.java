package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.LineBranch;
import com.gdamiens.website.controller.object.v2.LineDetail;
import com.gdamiens.website.controller.object.v2.LineDirection;
import com.gdamiens.website.controller.object.v2.StopRef;
import com.gdamiens.website.controller.object.v2.Vehicle;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.DatedVehicleJourneyRef;
import com.gdamiens.website.idfm.DestinationDisplay;
import com.gdamiens.website.idfm.DestinationName;
import com.gdamiens.website.idfm.DestinationRef;
import com.gdamiens.website.idfm.EstimatedCall;
import com.gdamiens.website.idfm.EstimatedCalls;
import com.gdamiens.website.idfm.EstimatedVehicleJourney;
import com.gdamiens.website.idfm.StopPointRef;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.utils.Constants;
import com.gdamiens.website.utils.TtlCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Vehicles of a line on its map, estimated from the PRIM estimated-timetable. Cached 30 s per line.
 * <p>
 * Two shapes of data come out of it:
 * <ul>
 *     <li>real vehicle journeys (SNCF, Métro 14…): each journey's next calls give its branch, the stop just left
 *     and, from the time to the next stop, the progress between the two;</li>
 *     <li>next passages per stop under made-up journeys mixing several vehicles (most RATP lines, buses): vehicles
 *     are found stop by stop, a vehicle being between two stops when the next one sees a passage before the
 *     previous one (see {@link #fromStops}).</li>
 * </ul>
 */
@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    /** Calls a bit in the past are kept: the vehicle may still be at the stop */
    private static final Duration PAST_MARGIN = Duration.ofSeconds(30);

    /** Default time between two stops when it can't be told from the data */
    private static final Duration DEFAULT_SEGMENT = Duration.ofMinutes(2);

    private static final Duration MIN_SEGMENT = Duration.ofSeconds(45);

    private static final Duration MAX_SEGMENT = Duration.ofMinutes(10);

    /** Vehicles at the first stop of their branch are shown when leaving within this time */
    private static final Duration TERMINUS_WINDOW = Duration.ofMinutes(3);

    /** Share of journeys whose calls don't follow a branch above which journeys are taken as passages per stop */
    private static final double MIXED_JOURNEYS = 0.2;

    private final IDFMLineService idfmLineService;

    private final NetworkService networkService;

    private final NetworkRepository networkRepository;

    private final TtlCache<String, List<Vehicle>> cache = new TtlCache<>(Duration.ofSeconds(30), 500);

    public VehicleService(IDFMLineService idfmLineService, NetworkService networkService, NetworkRepository networkRepository) {
        this.idfmLineService = idfmLineService;
        this.networkService = networkService;
        this.networkRepository = networkRepository;
    }

    /**
     * @return vehicles of the line, empty when it is unknown or has no real time
     */
    public List<Vehicle> getVehicles(String lineId) {
        return cache.get(lineId, this::load);
    }

    /** A call of a journey at a stop area */
    private record TimedCall(String stopAreaId, Instant expected, Instant aimed) {
    }

    /**
     * A journey of the feed with its future calls in time order
     *
     * @param destinationId stop area of its destination, when known
     */
    private record Run(String id, String destination, String destinationId, List<TimedCall> calls) {
    }

    private List<Vehicle> load(String lineId) {
        Optional<LineDetail> detail = networkService.getLineDetail(lineId);
        if (detail.isEmpty()) {
            return List.of();
        }
        List<EstimatedVehicleJourney> journeys;
        try {
            journeys = idfmLineService.getEstimatedVehicleJourneys(lineId, Constants.IDFM_ESTIMATED_TIMETABLE_URL);
        } catch (CustomException e) {
            // No real time for this line
            log.info("No vehicles for line {}: {}", lineId, e.getMessage());
            return List.of();
        }

        // SIRI StopPointRef (STIF:StopPoint:Q:<id>:) is the GTFS quay IDFM:<id>, or IDFM:monomodalStopPlace:<id> (RER)
        Set<String> quayIds = new HashSet<>();
        journeys.forEach(journey -> {
            calls(journey).forEach(call -> Optional.ofNullable(quayId(call)).ifPresent(quayIds::add));
            Optional.ofNullable(destinationRef(journey)).ifPresent(quayIds::add);
        });
        quayIds.addAll(quayIds.stream().map(VehicleService::monomodal).toList());
        Map<String, String> quayStopAreas = networkRepository.findStopAreasOfQuays(quayIds);

        Instant now = Instant.now();
        List<Run> runs = journeys.stream().map(journey -> toRun(journey, quayStopAreas, now)).filter(run -> !run.calls().isEmpty()).toList();

        // Made-up journeys mixing vehicles: their calls don't follow a branch in time order
        List<Run> multiCall = runs.stream().filter(run -> run.calls().size() > 1).toList();
        long mixed = multiCall.stream().filter(run -> {
            Position position = locate(detail.get(), stopIds(run));
            return position == null || position.matched() < run.calls().size() - 1;
        }).count();
        boolean perStop = !multiCall.isEmpty() && mixed > multiCall.size() * MIXED_JOURNEYS;

        List<Vehicle> vehicles = perStop ? fromStops(detail.get(), runs, now) : fromJourneys(detail.get(), runs, now);
        log.info("{} vehicles on line {} ({} journeys, {})", vehicles.size(), lineId, journeys.size(), perStop ? "passages per stop" : "vehicle journeys");
        return vehicles;
    }

    private static Run toRun(EstimatedVehicleJourney journey, Map<String, String> quayStopAreas, Instant now) {
        List<TimedCall> calls = calls(journey).stream()
            .map(call -> {
                String stopAreaId = stopArea(quayId(call), quayStopAreas);
                Instant expected = Optional.ofNullable(parse(call.getExpectedArrivalTime())).orElse(parse(call.getExpectedDepartureTime()));
                Instant aimed = Optional.ofNullable(parse(call.getAimedArrivalTime())).orElse(parse(call.getAimedDepartureTime()));
                return stopAreaId == null || expected == null ? null : new TimedCall(stopAreaId, expected, aimed);
            })
            .filter(Objects::nonNull)
            .filter(call -> call.expected().isAfter(now.minus(PAST_MARGIN)))
            .sorted(Comparator.comparing(TimedCall::expected))
            .toList();
        String id = Optional.ofNullable(journey.getDatedVehicleJourneyRef()).map(DatedVehicleJourneyRef::getValue).orElse(null);
        String destinationRef = destinationRef(journey);
        // A stop area ref (STIF:StopArea:SP:<id>:) may directly be the GTFS one
        String destinationId = destinationRef == null ? null
            : Optional.ofNullable(stopArea(destinationRef, quayStopAreas)).orElse(destinationRef);
        return new Run(id, destination(journey), destinationId, calls);
    }

    /**
     * Vehicles from real vehicle journeys: each one is placed from its next calls
     */
    private static List<Vehicle> fromJourneys(LineDetail detail, List<Run> runs, Instant now) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (Run run : runs) {
            toVehicle(run, detail, now).ifPresent(vehicles::add);
        }
        return vehicles;
    }

    private static Optional<Vehicle> toVehicle(Run run, LineDetail detail, Instant now) {
        List<TimedCall> calls = run.calls();
        TimedCall next = calls.getFirst();
        TimedCall following = calls.size() > 1 ? calls.get(1) : null;

        // Branch serving the next stop then most of the following ones, in order (branches share their trunk)
        Position position = locate(detail, stopIds(run));
        if (position == null) {
            return Optional.empty();
        }
        LineBranch branch = detail.directions().get(position.direction()).branches().get(position.branch());
        StopRef from = position.index() > 0 ? branch.stops().get(position.index() - 1) : null;
        StopRef to = branch.stops().get(position.index());

        Duration toNext = Duration.between(now, next.expected());
        double progress;
        if (from == null) {
            // Waiting at the first stop of the branch
            if (toNext.compareTo(TERMINUS_WINDOW) > 0) {
                return Optional.empty();
            }
            progress = 1;
        } else {
            Duration segment = following == null
                ? DEFAULT_SEGMENT
                : clamp(Duration.between(next.expected(), following.expected()), Duration.ofMinutes(1), MAX_SEGMENT);
            if (toNext.compareTo(segment.multipliedBy(2)) > 0) {
                // Far from the next stop: not running yet, or a gap in the data
                return Optional.empty();
            }
            progress = progress(toNext, segment);
        }

        String id = Optional.ofNullable(run.id()).orElse(position.direction() + ":" + position.branch() + ":" + next.expected());
        return Optional.of(vehicle(id, run, branch, position.direction(), position.branch(), position.index(), from, next, progress));
    }

    /** A passage at a stop of a branch */
    private record Passage(TimedCall call, Run run) {
    }

    /**
     * Vehicles from passages per stop, along each branch: a passage at a stop before the next passage at the previous
     * stop (give or take half the time between the two) is a vehicle that has already left the previous one. The time
     * between two stops is the shortest gap seen between a passage at the previous stop and the next one at this stop.
     */
    private static List<Vehicle> fromStops(LineDetail detail, List<Run> runs, Instant now) {
        // Passages per stop area, the same one listed by several journeys (quays of both sides) counted once
        Map<String, List<Passage>> byStop = new HashMap<>();
        Set<String> seen = new HashSet<>();
        for (Run run : runs) {
            for (TimedCall call : run.calls()) {
                if (seen.add(call.stopAreaId() + "|" + call.expected() + "|" + run.destinationId() + "|" + run.destination())) {
                    byStop.computeIfAbsent(call.stopAreaId(), id -> new ArrayList<>()).add(new Passage(call, run));
                }
            }
        }

        // One vehicle per direction, segment and time: on a trunk, kept on the branch going to its destination
        Map<String, Vehicle> vehicles = new LinkedHashMap<>();
        Set<String> onOwnBranch = new HashSet<>();
        List<LineDirection> directions = detail.directions();
        for (int d = 0; d < directions.size(); d++) {
            List<LineBranch> branches = directions.get(d).branches();
            for (int b = 0; b < branches.size(); b++) {
                LineBranch branch = branches.get(b);
                List<StopRef> stops = branch.stops();
                int last = stops.size() - 1;
                Set<String> served = new HashSet<>();
                Set<String> repeated = new HashSet<>();
                stops.forEach(stop -> {
                    if (!served.add(stop.id())) {
                        repeated.add(stop.id());
                    }
                });
                List<List<Passage>> passages = new ArrayList<>();
                for (int i = 0; i < stops.size(); i++) {
                    int index = i;
                    passages.add(byStop.getOrDefault(stops.get(i).id(), List.of()).stream()
                        .filter(passage -> {
                            // Going further on this branch (or ending here, but not at its first stop)
                            int to = destinationIndex(branch, passage.run(), index);
                            return to > index || (to == index && index > 0);
                        })
                        .sorted(Comparator.comparing(passage -> passage.call().expected()))
                        .toList());
                }

                for (int i = 0; i < stops.size(); i++) {
                    List<Passage> here = passages.get(i);
                    if (here.isEmpty()) {
                        continue;
                    }
                    if (i == 0) {
                        // Waiting at the first stop
                        Passage first = here.getFirst();
                        if (Duration.between(now, first.call().expected()).compareTo(TERMINUS_WINDOW) <= 0) {
                            add(vehicles, onOwnBranch, vehicle(null, first.run(), branch, d, b, 0, null, first.call(), 1),
                                destinationIndex(branch, first.run(), 0) == last);
                        }
                        continue;
                    }
                    StopRef from = stops.get(i - 1);
                    if (repeated.contains(stops.get(i).id())) {
                        // Served several times (loop): its passages can't be told apart
                        continue;
                    }
                    List<Passage> before = passages.get(i - 1);
                    Duration segment = segment(before, here);
                    for (Passage passage : here) {
                        Instant expected = passage.call().expected();
                        // Without passages at the previous stop, only the first one here is known to be between them
                        boolean between = before.isEmpty()
                            ? passage == here.getFirst()
                            : expected.isBefore(before.getFirst().call().expected().plus(segment.dividedBy(2)));
                        if (!between) {
                            break;
                        }
                        Duration toNext = Duration.between(now, expected);
                        if (toNext.compareTo(segment.multipliedBy(2).plusMinutes(1)) > 0) {
                            // Not coming from the previous stop (skipped it, or a gap in the data)
                            break;
                        }
                        add(vehicles, onOwnBranch, vehicle(null, passage.run(), branch, d, b, i, from, passage.call(), progress(toNext, segment)),
                            destinationIndex(branch, passage.run(), i) == last);
                    }
                }
            }
        }
        return new ArrayList<>(vehicles.values());
    }

    /**
     * @param own the branch ends at the vehicle's destination
     */
    private static void add(Map<String, Vehicle> vehicles, Set<String> onOwnBranch, Vehicle vehicle, boolean own) {
        String key = vehicle.direction() + "|" + vehicle.fromStopId() + "|" + vehicle.toStopId() + "|" + vehicle.expectedAt();
        if (!vehicles.containsKey(key) || (own && !onOwnBranch.contains(key))) {
            vehicles.put(key, vehicle);
            if (own) {
                onOwnBranch.add(key);
            }
        }
    }

    /** Shortest time from a passage at the previous stop to the next one at this stop */
    private static Duration segment(List<Passage> before, List<Passage> here) {
        Duration shortest = null;
        for (Passage passage : here) {
            Instant expected = passage.call().expected();
            Instant left = null;
            for (Passage previous : before) {
                if (previous.call().expected().isAfter(expected)) {
                    break;
                }
                left = previous.call().expected();
            }
            if (left != null) {
                Duration gap = Duration.between(left, expected);
                if (gap.isPositive() && gap.compareTo(MAX_SEGMENT) <= 0 && (shortest == null || gap.compareTo(shortest) < 0)) {
                    shortest = gap;
                }
            }
        }
        return shortest == null ? DEFAULT_SEGMENT : clamp(shortest, MIN_SEGMENT, MAX_SEGMENT);
    }

    private static double progress(Duration toNext, Duration segment) {
        return Math.clamp(1 - (double) toNext.toSeconds() / segment.toSeconds(), 0, 1);
    }

    /**
     * @param id    journey id, null for a vehicle found from passages per stop (an id is made from where it is)
     * @param index position of the next stop in the branch
     */
    private static Vehicle vehicle(String id, Run run, LineBranch branch, int direction, int branchIndex, int index, StopRef from,
                                   TimedCall next, double progress) {
        StopRef to = branch.stops().get(index);
        int destination = destinationIndex(branch, run, index);
        if (id == null) {
            id = "%d:%s:%s".formatted(direction, to.id(), next.expected().getEpochSecond());
        }
        Integer delay = next.aimed() == null ? null : (int) Duration.between(next.aimed(), next.expected()).toSeconds();
        // The announced destination when it is a stop ahead on the branch (it is sometimes the other end of the line),
        // the branch's terminus otherwise
        String name = destination >= 0 && run.destination() != null ? run.destination() : branch.stops().getLast().name();
        return new Vehicle(id, name, direction, branchIndex, from == null ? null : from.id(), to.id(), to.name(), progress,
            next.expected(), delay);
    }

    /**
     * Position of the run's destination in the branch from [from] on, by stop area then by name; -1 when it is not
     * ahead on the branch
     */
    private static int destinationIndex(LineBranch branch, Run run, int from) {
        List<StopRef> stops = branch.stops();
        if (run.destinationId() != null) {
            for (int i = from; i < stops.size(); i++) {
                if (stops.get(i).id().equals(run.destinationId())) {
                    return i;
                }
            }
        }
        String key = normalize(run.destination());
        if (key.isEmpty()) {
            return -1;
        }
        for (int i = from; i < stops.size(); i++) {
            String name = normalize(stops.get(i).name());
            if (!name.isEmpty() && (name.contains(key) || key.contains(name))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param index     position of the next stop in the branch
     * @param lastIndex position of the last call found after it
     */
    private record Position(int direction, int branch, int index, int lastIndex, int matched) {
    }

    private static List<String> stopIds(Run run) {
        return run.calls().stream().map(TimedCall::stopAreaId).toList();
    }

    /**
     * Branch serving the first stop of [stopIds] and, in order after it, the most of the others; null when none
     * serves the first one or a following one (a vehicle needs a direction)
     */
    private static Position locate(LineDetail detail, List<String> stopIds) {
        Position best = null;
        List<LineDirection> directions = detail.directions();
        for (int d = 0; d < directions.size(); d++) {
            List<LineBranch> branches = directions.get(d).branches();
            for (int b = 0; b < branches.size(); b++) {
                List<StopRef> stops = branches.get(b).stops();
                for (int i = 0; i < stops.size(); i++) {
                    if (!stops.get(i).id().equals(stopIds.getFirst())) {
                        continue;
                    }
                    // Following calls found in order after the next stop (skipped stops allowed)
                    int matched = 0;
                    int last = i;
                    for (String stopId : stopIds.subList(1, stopIds.size())) {
                        for (int j = last + 1; j < stops.size(); j++) {
                            if (stops.get(j).id().equals(stopId)) {
                                matched++;
                                last = j;
                                break;
                            }
                        }
                    }
                    // Same match: a vehicle coming from somewhere rather than leaving the first stop (a train whose
                    // only call left is its terminus is arriving there)
                    if (best == null || matched > best.matched() || (matched == best.matched() && best.index() == 0 && i > 0)) {
                        best = new Position(d, b, i, last, matched);
                    }
                }
            }
        }
        return best == null || (stopIds.size() > 1 && best.matched() == 0) ? null : best;
    }

    private static List<EstimatedCall> calls(EstimatedVehicleJourney journey) {
        return Optional.ofNullable(journey.getEstimatedCalls()).map(EstimatedCalls::getEstimatedCall).orElse(List.of());
    }

    private static String quayId(EstimatedCall call) {
        return gtfsId(Optional.ofNullable(call.getStopPointRef()).map(StopPointRef::getValue).orElse(null));
    }

    private static String destinationRef(EstimatedVehicleJourney journey) {
        return gtfsId(Optional.ofNullable(journey.getDestinationRef()).map(DestinationRef::getValue).orElse(null));
    }

    /** STIF:StopPoint:Q:<id>: or STIF:StopArea:SP:<id>: → IDFM:<id> */
    private static String gtfsId(String ref) {
        String[] parts = ref == null ? new String[0] : ref.split(":");
        return parts.length > 3 ? "IDFM:" + parts[3] : null;
    }

    private static String monomodal(String quayId) {
        return "IDFM:monomodalStopPlace:" + quayId.substring("IDFM:".length());
    }

    private static String stopArea(String quayId, Map<String, String> quayStopAreas) {
        return quayId == null ? null : quayStopAreas.getOrDefault(quayId, quayStopAreas.get(monomodal(quayId)));
    }

    private static String normalize(String name) {
        return name == null ? "" : StringUtils.stripAccents(name).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static String destination(EstimatedVehicleJourney journey) {
        String name = Optional.ofNullable(journey.getDestinationName()).flatMap(names -> names.stream().findFirst())
            .map(DestinationName::getValue).orElse(null);
        if (StringUtils.isNotBlank(name)) {
            return name;
        }
        return calls(journey).stream()
            .map(EstimatedCall::getDestinationDisplay)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .map(DestinationDisplay::getValue)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(null);
    }

    private static Duration clamp(Duration value, Duration min, Duration max) {
        return value.compareTo(min) < 0 ? min : value.compareTo(max) > 0 ? max : value;
    }

    private static Instant parse(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
