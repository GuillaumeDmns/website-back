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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Vehicles of a line on its map, estimated from the PRIM estimated-timetable (next calls of each vehicle journey):
 * the next stop and the one after give the branch and the stop just left, the time to the next stop gives the
 * progress between the two. Cached 30 s per line.
 */
@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    /** Calls a bit in the past are kept: the vehicle may still be at the stop */
    private static final Duration PAST_MARGIN = Duration.ofSeconds(30);

    /** Default time between two stops when the following call is unknown */
    private static final Duration DEFAULT_SEGMENT = Duration.ofMinutes(2);

    /** Vehicles at the first stop of their branch are shown when leaving within this time */
    private static final Duration TERMINUS_WINDOW = Duration.ofMinutes(3);

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
        journeys.forEach(journey -> calls(journey).forEach(call -> Optional.ofNullable(quayId(call)).ifPresent(id -> {
            quayIds.add(id);
            quayIds.add(monomodal(id));
        })));
        Map<String, String> quayStopAreas = networkRepository.findStopAreasOfQuays(quayIds);
        Map<String, String> stopAreas = new HashMap<>();
        quayIds.stream().filter(id -> !id.contains("monomodal")).forEach(id -> {
            String stopArea = quayStopAreas.getOrDefault(id, quayStopAreas.get(monomodal(id)));
            if (stopArea != null) {
                stopAreas.put(id, stopArea);
            }
        });

        Instant now = Instant.now();
        List<Vehicle> vehicles = new ArrayList<>();
        for (EstimatedVehicleJourney journey : journeys) {
            toVehicle(journey, detail.get(), stopAreas, now).ifPresent(vehicles::add);
        }
        log.info("{} vehicles on line {} ({} journeys)", vehicles.size(), lineId, journeys.size());
        return vehicles;
    }

    private record TimedCall(String stopAreaId, Instant expected, Instant aimed) {
    }

    private Optional<Vehicle> toVehicle(EstimatedVehicleJourney journey, LineDetail detail, Map<String, String> stopAreas, Instant now) {
        List<TimedCall> calls = calls(journey).stream()
            .map(call -> {
                String stopAreaId = stopAreas.get(quayId(call));
                Instant expected = Optional.ofNullable(parse(call.getExpectedArrivalTime())).orElse(parse(call.getExpectedDepartureTime()));
                Instant aimed = Optional.ofNullable(parse(call.getAimedArrivalTime())).orElse(parse(call.getAimedDepartureTime()));
                return stopAreaId == null || expected == null ? null : new TimedCall(stopAreaId, expected, aimed);
            })
            .filter(Objects::nonNull)
            .filter(call -> call.expected().isAfter(now.minus(PAST_MARGIN)))
            .sorted(Comparator.comparing(TimedCall::expected))
            .toList();
        if (calls.isEmpty()) {
            return Optional.empty();
        }
        TimedCall next = calls.getFirst();
        TimedCall following = calls.size() > 1 ? calls.get(1) : null;

        // Branch serving the next stop then most of the following ones, in order (branches share their trunk)
        Position position = locate(detail, calls.stream().map(TimedCall::stopAreaId).toList());
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
                : clamp(Duration.between(next.expected(), following.expected()), Duration.ofMinutes(1), Duration.ofMinutes(10));
            if (toNext.compareTo(segment.multipliedBy(2)) > 0) {
                // Far from the next stop: not running yet, or a gap in the data
                return Optional.empty();
            }
            progress = Math.clamp(1 - (double) toNext.toSeconds() / segment.toSeconds(), 0, 1);
        }

        Integer delay = next.aimed() == null ? null : (int) Duration.between(next.aimed(), next.expected()).toSeconds();
        String id = Optional.ofNullable(journey.getDatedVehicleJourneyRef()).map(DatedVehicleJourneyRef::getValue)
            .orElse(position.direction() + ":" + position.branch() + ":" + next.expected());
        String destination = destination(journey, branch, position.index());
        return Optional.of(new Vehicle(id, destination, position.direction(), position.branch(),
            from == null ? null : from.id(), to.id(), to.name(), progress, next.expected(), delay));
    }

    /**
     * @param index     position of the next stop in the branch
     * @param lastIndex position of the last call found after it
     */
    private record Position(int direction, int branch, int index, int lastIndex, int matched) {
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
        String ref = Optional.ofNullable(call.getStopPointRef()).map(StopPointRef::getValue).orElse(null);
        String[] parts = ref == null ? new String[0] : ref.split(":");
        return parts.length > 3 ? "IDFM:" + parts[3] : null;
    }

    private static String monomodal(String quayId) {
        return "IDFM:monomodalStopPlace:" + quayId.substring("IDFM:".length());
    }

    /**
     * The announced destination when it is a stop ahead on the branch (it is sometimes the other end of the line),
     * the branch's terminus otherwise
     */
    private static String destination(EstimatedVehicleJourney journey, LineBranch branch, int index) {
        String announced = destination(journey);
        String key = normalize(announced);
        boolean ahead = !key.isEmpty() && branch.stops().subList(index, branch.stops().size()).stream()
            .map(stop -> normalize(stop.name()))
            .anyMatch(name -> !name.isEmpty() && (name.contains(key) || key.contains(name)));
        return ahead ? announced : branch.stops().getLast().name();
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
