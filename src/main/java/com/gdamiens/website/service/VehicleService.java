package com.gdamiens.website.service;

import com.gdamiens.website.client.PrimRealtimeClient;
import com.gdamiens.website.controller.object.v2.LineBranch;
import com.gdamiens.website.controller.object.v2.LineDetail;
import com.gdamiens.website.controller.object.v2.LineDirection;
import com.gdamiens.website.controller.object.v2.StopRef;
import com.gdamiens.website.controller.object.v2.Vehicle;
import com.gdamiens.website.controller.object.v2.VehicleCall;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.exceptions.QuotaExceededException;
import com.gdamiens.website.client.siri.ArrivalPlatformName;
import com.gdamiens.website.client.siri.DatedVehicleJourneyRef;
import com.gdamiens.website.client.siri.DestinationDisplay;
import com.gdamiens.website.client.siri.DestinationName;
import com.gdamiens.website.client.siri.DestinationRef;
import com.gdamiens.website.client.siri.EstimatedCall;
import com.gdamiens.website.client.siri.EstimatedCalls;
import com.gdamiens.website.client.siri.EstimatedVehicleJourney;
import com.gdamiens.website.client.siri.JourneyNote;
import com.gdamiens.website.client.siri.RecordedCall;
import com.gdamiens.website.client.siri.RecordedCalls;
import com.gdamiens.website.client.siri.StopPointRef;
import com.gdamiens.website.client.siri.VehicleJourneyName;
import com.gdamiens.website.model.IDFMRoute;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.repository.NetworkRepository.ScheduledCallRow;
import com.gdamiens.website.utils.ParisTime;
import com.gdamiens.website.utils.TtlCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Vehicles of a line on its map, estimated from the PRIM estimated-timetable. Cached 60 s per line.
 * <p>
 * Two shapes of data come out of it:
 * <ul>
 *     <li>real vehicle journeys (SNCF, Métro 14…): each journey's next calls give its next stop; the stop it left
 *     comes from its calls already made when the feed gives them, else from its trip in the GTFS (same train number
 *     or same scheduled times: the stops it serves, so that skipped stops and the branch it comes from are known),
 *     else it is the stop before on the branch. The progress between the two comes from the time to the next
 *     stop. Real time always wins: the GTFS only tells where it comes from and how long that ride usually takes;</li>
 *     <li>next passages per stop under made-up journeys mixing several vehicles (most RATP lines, buses): vehicles
 *     are followed from stop to stop along each branch (see {@link #fromStops}).</li>
 * </ul>
 * Stops the line doesn't serve now (closed by a blocking disruption, see {@link TrafficService#getClosedStops}) are
 * never taken as the stop a vehicle left, and are skipped when following vehicles stop by stop.
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

    /** Two passages of a stop closer than this are the same vehicle; a stop may see a vehicle that much before the previous one */
    private static final Duration SAME_VEHICLE = Duration.ofSeconds(30);

    /** Vehicles at the first stop of their branch are shown when leaving within this time */
    private static final Duration TERMINUS_WINDOW = Duration.ofMinutes(3);

    /** Share of journeys whose calls don't follow a branch above which journeys are taken as passages per stop */
    private static final double MIXED_JOURNEYS = 0.2;

    /** A scheduled call and a real-time one at the same stop are the same trip when their scheduled times are this close */
    private static final Duration SCHEDULE_TOLERANCE = Duration.ofSeconds(90);

    /** The same train number is the same trip within this time (one number a day, the schedule may have changed since) */
    private static final Duration NUMBER_TOLERANCE = Duration.ofMinutes(30);

    /** Longest ride between two stops served (direct trains) */
    private static final Duration MAX_RIDE = Duration.ofMinutes(30);

    /** A vehicle not gone yet from the stop it left (held there) is shown there for this long after its departure time */
    private static final Duration HELD = Duration.ofMinutes(5);

    /** From this hour, tomorrow's scheduled trips are read too */
    private static final int EVENING = 20;

    /** A vehicle missing from the feed is kept for that many loads, moved along from its last known position */
    private static final int MAX_MISSED = 2;

    /** A vehicle of the previous load is the same as one of this load when they are expected at a stop this close */
    private static final Duration SAME_CALL = Duration.ofSeconds(60);

    private final PrimRealtimeClient primRealtimeClient;

    private final NetworkService networkService;

    private final NetworkRepository networkRepository;

    private final TrafficService trafficService;

    /** Scheduled trips of a line over a service day, for the lines with real vehicle journeys */
    private final TtlCache<ScheduleKey, Schedule> schedules = new TtlCache<>(Duration.ofHours(6), 100);

    /** Vehicles of each line's last load, to keep the ones missing for a while and their ids */
    private final Map<String, Previous> previous = new ConcurrentHashMap<>();

    /** A minute: the estimated-timetable has a small daily quota (see {@link ApiQuota}) */
    private final TtlCache<String, List<Vehicle>> cache = new TtlCache<>(Duration.ofSeconds(60), 500);

    public VehicleService(PrimRealtimeClient primRealtimeClient, NetworkService networkService, NetworkRepository networkRepository,
                          TrafficService trafficService) {
        this.primRealtimeClient = primRealtimeClient;
        this.networkService = networkService;
        this.networkRepository = networkRepository;
        this.trafficService = trafficService;
    }

    /** Vehicles are placed on the line's branches and scheduled trips: both may have changed */
    @EventListener(GtfsImportedEvent.class)
    public void onGtfsImported() {
        schedules.invalidateAll();
        cache.invalidateAll();
    }

    /**
     * @return vehicles of the line, empty when it is unknown or has no real time
     */
    public List<Vehicle> getVehicles(String lineId) {
        try {
            return cache.get(lineId, this::load);
        } catch (QuotaExceededException e) {
            // The guests' share is used up: no vehicles for this caller, not cached so that accounts keep them
            return List.of();
        }
    }

    /** A call of a journey at a stop area */
    private record TimedCall(String stopAreaId, Instant expected, Instant aimed, String platform) {
    }

    /** The stop a vehicle left, and when; a null stop for a vehicle whose trip starts at its next stop */
    private record Left(String stopAreaId, Instant at) {
    }

    /**
     * A journey of the feed with its future calls in time order
     *
     * @param name          mission code or train number, when given
     * @param number        SNCF train number, when given
     * @param destinationId stop area of its destination, when known
     * @param recorded      its last call already made, when the feed gives them
     */
    private record Run(String id, String name, String number, String destination, String destinationId, List<TimedCall> calls,
                       Left recorded) {
    }

    private record ScheduleKey(String lineId, LocalDate date) {
    }

    private record ScheduledStop(String stopAreaId, Instant arrival, Instant departure) {
    }

    /** A trip of the GTFS and the stops it serves, in order */
    private record ScheduledTrip(String number, List<ScheduledStop> stops) {
    }

    /** The stop at [index] of a trip */
    private record TripStop(ScheduledTrip trip, int index) {

        ScheduledStop stop() {
            return trip.stops().get(index);
        }
    }

    /** Trips of a line over a service day, by the stop areas they serve */
    private record Schedule(Map<String, List<TripStop>> byStop) {
    }

    private List<Vehicle> load(String lineId) {
        Optional<LineDetail> detail = networkService.getLineDetail(lineId);
        if (detail.isEmpty()) {
            return List.of();
        }
        List<EstimatedVehicleJourney> journeys;
        try {
            journeys = primRealtimeClient.getEstimatedVehicleJourneys(lineId);
        } catch (QuotaExceededException e) {
            if (e.isGuestShare()) {
                throw e;
            }
            log.info("No vehicles for line {}: {}", lineId, e.getMessage());
            return List.of();
        } catch (CustomException e) {
            // No real time for this line
            log.info("No vehicles for line {}: {}", lineId, e.getMessage());
            return List.of();
        }

        // SIRI StopPointRef (STIF:StopPoint:Q:<id>:) is the GTFS quay IDFM:<id>, or IDFM:monomodalStopPlace:<id> (RER)
        Set<String> quayIds = new HashSet<>();
        journeys.forEach(journey -> {
            calls(journey).forEach(call -> Optional.ofNullable(quayId(call)).ifPresent(quayIds::add));
            recordedCalls(journey).forEach(call -> Optional.ofNullable(quayId(call)).ifPresent(quayIds::add));
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

        Set<String> closed = trafficService.getClosedStops(lineId);
        List<Vehicle> vehicles = perStop
            ? fromStops(detail.get(), runs, closed, now)
            : fromJourneys(detail.get(), runs, schedules(lineId, now), closed, now);
        vehicles = withPrevious(lineId, vehicles, perStop, now);
        log.info("{} vehicles on line {} ({} journeys, {})", vehicles.size(), lineId, journeys.size(), perStop ? "passages per stop" : "vehicle journeys");
        return vehicles;
    }

    /** A vehicle of a load, and how many loads in a row it has been missing from the feed since */
    private record Kept(Vehicle vehicle, int missed) {
    }

    private record Previous(Instant at, List<Kept> vehicles) {
    }

    /**
     * The vehicles of this load, with the ids they had in the previous one (vehicles followed stop by stop get a new
     * one at each stop: [perStop], they are recognised by their calls), and those of the previous load missing from
     * this one (the feed sometimes misses a vehicle for a minute or two), moved along, for at most
     * {@link #MAX_MISSED} loads and until their next stop
     */
    private List<Vehicle> withPrevious(String lineId, List<Vehicle> loaded, boolean perStop, Instant now) {
        Previous before = previous.get(lineId);
        List<Kept> candidates = before == null || before.at().isBefore(now.minus(Duration.ofMinutes(5))) ? List.of() : before.vehicles();
        Set<Kept> found = new HashSet<>();
        List<Kept> kept = new ArrayList<>();
        for (Vehicle vehicle : loaded) {
            Vehicle current = vehicle;
            Kept same = candidates.stream().filter(candidate -> !found.contains(candidate) && candidate.vehicle().id().equals(current.id()))
                .findFirst()
                .or(() -> !perStop ? Optional.empty() : candidates.stream()
                    .filter(candidate -> !found.contains(candidate) && sameCalls(candidate.vehicle(), current)).findFirst())
                .orElse(null);
            if (same != null) {
                found.add(same);
                vehicle = withId(vehicle, same.vehicle().id());
            }
            kept.add(new Kept(vehicle, 0));
        }
        for (Kept candidate : candidates) {
            Vehicle vehicle = candidate.vehicle();
            if (!found.contains(candidate) && candidate.missed() < MAX_MISSED && vehicle.expectedAt().isAfter(now.minus(PAST_MARGIN))) {
                kept.add(new Kept(moved(vehicle, before.at(), now), candidate.missed() + 1));
            }
        }
        previous.put(lineId, new Previous(now, kept));
        return kept.stream().map(Kept::vehicle).toList();
    }

    /** Same direction, expected at one of the same stops at about the same time */
    private static boolean sameCalls(Vehicle before, Vehicle now) {
        if (before.direction() != now.direction()) {
            return false;
        }
        Map<String, Instant> times = new HashMap<>();
        before.calls().forEach(call -> times.put(call.stopId(), call.expectedAt()));
        return now.calls().stream().anyMatch(call -> times.containsKey(call.stopId())
            && Duration.between(times.get(call.stopId()), call.expectedAt()).abs().compareTo(SAME_CALL) <= 0);
    }

    private static Vehicle withId(Vehicle vehicle, String id) {
        return new Vehicle(id, vehicle.name(), vehicle.destination(), vehicle.direction(), vehicle.branch(), vehicle.fromStopId(),
            vehicle.toStopId(), vehicle.toStopName(), vehicle.progress(), vehicle.expectedAt(), vehicle.delaySeconds(), vehicle.calls());
    }

    /** [vehicle] as loaded at [at], moved along its segment at the same pace until [now] */
    private static Vehicle moved(Vehicle vehicle, Instant at, Instant now) {
        double progress = vehicle.progress();
        long remainingThen = Duration.between(at, vehicle.expectedAt()).toMillis();
        if (vehicle.fromStopId() != null && progress < 1 && remainingThen > 0) {
            double segment = remainingThen / (1 - progress);
            progress = Math.clamp(1 - Duration.between(now, vehicle.expectedAt()).toMillis() / segment, progress, 1);
        }
        List<VehicleCall> calls = vehicle.calls().stream().filter(call -> call.expectedAt().isAfter(now.minus(PAST_MARGIN))).toList();
        return new Vehicle(vehicle.id(), vehicle.name(), vehicle.destination(), vehicle.direction(), vehicle.branch(), vehicle.fromStopId(),
            vehicle.toStopId(), vehicle.toStopName(), progress, vehicle.expectedAt(), vehicle.delaySeconds(), calls);
    }

    private static Run toRun(EstimatedVehicleJourney journey, Map<String, String> quayStopAreas, Instant now) {
        List<TimedCall> calls = calls(journey).stream()
            // A stop the vehicle won't make
            .filter(call -> !cancelled(call))
            .map(call -> {
                String stopAreaId = stopArea(quayId(call), quayStopAreas);
                Instant expected = Optional.ofNullable(parse(call.getExpectedArrivalTime())).orElse(parse(call.getExpectedDepartureTime()));
                Instant aimed = Optional.ofNullable(parse(call.getAimedArrivalTime())).orElse(parse(call.getAimedDepartureTime()));
                String platform = Optional.ofNullable(call.getArrivalPlatformName()).map(ArrivalPlatformName::getValue)
                    .filter(StringUtils::isNotBlank).orElse(null);
                return stopAreaId == null || expected == null ? null : new TimedCall(stopAreaId, expected, aimed, platform);
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
        String name = Optional.ofNullable(journey.getVehicleJourneyName()).orElse(List.of()).stream()
            .map(VehicleJourneyName::getValue).filter(StringUtils::isNotBlank).findFirst()
            .or(() -> Optional.ofNullable(journey.getJourneyNote()).orElse(List.of()).stream()
                .map(JourneyNote::getValue).filter(StringUtils::isNotBlank).findFirst())
            .orElse(null);
        String number = Stream.concat(
                Optional.ofNullable(journey.getVehicleJourneyName()).orElse(List.of()).stream().map(VehicleJourneyName::getValue),
                Optional.ofNullable(journey.getJourneyNote()).orElse(List.of()).stream().map(JourneyNote::getValue))
            .filter(DepartureService::isTrainNumber)
            .findFirst().orElse(null);
        return new Run(id, name, number, destination(journey), destinationId, calls, recorded(journey, quayStopAreas));
    }

    /** Every status given for the call says it is cancelled */
    private static boolean cancelled(EstimatedCall call) {
        List<String> statuses = Stream.of(call.getArrivalStatus(), call.getDepartureStatus()).filter(Objects::nonNull).toList();
        return !statuses.isEmpty() && statuses.stream().allMatch("cancelled"::equalsIgnoreCase);
    }

    /** The last call already made, at a known stop, with when the vehicle left it */
    private static Left recorded(EstimatedVehicleJourney journey, Map<String, String> quayStopAreas) {
        return recordedCalls(journey).stream()
            .map(call -> {
                String stopAreaId = stopArea(quayId(call), quayStopAreas);
                Instant at = Stream.of(call.getActualDepartureTime(), call.getExpectedDepartureTime(), call.getActualArrivalTime(),
                    call.getAimedDepartureTime(), call.getAimedArrivalTime()).map(VehicleService::parse).filter(Objects::nonNull)
                    .findFirst().orElse(null);
                return stopAreaId == null || at == null ? null : new Left(stopAreaId, at);
            })
            .filter(Objects::nonNull)
            .max(Comparator.comparing(Left::at))
            .orElse(null);
    }

    /**
     * Vehicles from real vehicle journeys: each one is placed from its next calls
     */
    private static List<Vehicle> fromJourneys(LineDetail detail, List<Run> runs, List<Schedule> schedules, Set<String> closed, Instant now) {
        List<Vehicle> vehicles = new ArrayList<>();
        // Names of the line's stops, for the calls
        Map<String, String> names = new HashMap<>();
        detail.directions().forEach(direction -> direction.branches()
            .forEach(branch -> branch.stops().forEach(stop -> names.putIfAbsent(stop.id(), stop.name()))));
        int recorded = 0;
        int scheduled = 0;
        for (Run run : runs) {
            // Where it comes from: the stop the feed says it left, else the one before in its scheduled trip
            Left left = Optional.ofNullable(run.recorded()).orElseGet(() -> scheduledLeft(run, schedules, closed));
            if (run.recorded() != null) {
                recorded++;
            } else if (left != null) {
                scheduled++;
            }
            toVehicle(run, detail, left, calls(run, names), now).ifPresent(vehicles::add);
        }
        log.info("{} journeys of line {}: {} with their calls made, {} matched with their scheduled trip", runs.size(),
            detail.line().id(), recorded, scheduled);
        return vehicles;
    }

    /**
     * Scheduled trips of the line on the service days running now: today's, yesterday's after midnight, and in the
     * evening tomorrow's (trains leaving after midnight belong to the next day in the SNCF's GTFS)
     */
    private List<Schedule> schedules(String lineId, Instant now) {
        ZonedDateTime local = now.atZone(ParisTime.ZONE);
        LocalDate today = local.toLocalDate();
        List<LocalDate> dates = local.getHour() >= EVENING ? List.of(today, today.minusDays(1), today.plusDays(1))
            : List.of(today, today.minusDays(1));
        List<Schedule> loaded = new ArrayList<>();
        for (LocalDate date : dates) {
            try {
                loaded.add(schedules.get(new ScheduleKey(lineId, date), this::loadSchedule));
            } catch (RuntimeException e) {
                // Vehicles are then placed from the real time only
                log.warn("No schedule of line {} on {}: {}", lineId, date, e.getMessage());
            }
        }
        return loaded;
    }

    private Schedule loadSchedule(ScheduleKey key) {
        Map<String, List<TripStop>> byStop = new HashMap<>();
        List<ScheduledCallRow> rows = networkRepository.findScheduledCalls(IDFMRoute.toRouteId(key.lineId()), key.date());
        int start = 0;
        for (int i = 1; i <= rows.size(); i++) {
            if (i < rows.size() && rows.get(i).tripId().equals(rows.get(start).tripId())) {
                continue;
            }
            List<ScheduledStop> stops = rows.subList(start, i).stream()
                .map(row -> new ScheduledStop(row.stopAreaId(), ParisTime.ofServiceDay(key.date(), row.arrivalSeconds()),
                    ParisTime.ofServiceDay(key.date(), row.departureSeconds())))
                .toList();
            ScheduledTrip trip = new ScheduledTrip(StringUtils.trimToNull(rows.get(start).number()), stops);
            for (int k = 0; k < stops.size(); k++) {
                byStop.computeIfAbsent(stops.get(k).stopAreaId(), id -> new ArrayList<>()).add(new TripStop(trip, k));
            }
            start = i;
        }
        return new Schedule(byStop);
    }

    /**
     * The stop the run left according to its scheduled trip: the stop served before its next one (closed stops
     * skipped), at its scheduled departure shifted by the delay at the next stop; null when no trip matches
     */
    private static Left scheduledLeft(Run run, List<Schedule> schedules, Set<String> closed) {
        TripStop match = scheduledTrip(run, schedules);
        if (match == null) {
            return null;
        }
        if (match.index() == 0) {
            return new Left(null, null);
        }
        List<ScheduledStop> stops = match.trip().stops();
        int k = match.index() - 1;
        while (k > 0 && closed.contains(stops.get(k).stopAreaId())) {
            k--;
        }
        Duration delay = Duration.between(match.stop().arrival(), run.calls().getFirst().expected());
        return new Left(stops.get(k).stopAreaId(), stops.get(k).departure().plus(delay));
    }

    /**
     * The scheduled trip of a run, at its next stop: the one with its train number, else the one whose scheduled
     * times match the run's at its next stop and the following ones (up to 3); null when none is clear
     */
    private static TripStop scheduledTrip(Run run, List<Schedule> schedules) {
        TimedCall next = run.calls().getFirst();
        List<TimedCall> following = run.calls().subList(1, Math.min(run.calls().size(), 4));
        TripStop best = null;
        Comparator<Candidate> order = Comparator.comparing(Candidate::sameNumber)
            .thenComparing(Candidate::matched)
            .thenComparing(Candidate::gap, Comparator.reverseOrder());
        Candidate bestCandidate = null;
        for (Schedule schedule : schedules) {
            for (TripStop candidate : schedule.byStop().getOrDefault(next.stopAreaId(), List.of())) {
                boolean sameNumber = run.number() != null && run.number().equals(candidate.trip().number());
                Duration gap = gap(candidate.stop(), next, sameNumber);
                if (gap == null || gap.compareTo(sameNumber ? NUMBER_TOLERANCE : SCHEDULE_TOLERANCE) > 0) {
                    continue;
                }
                // Its following calls served later by the trip, at the same scheduled times
                int matched = 0;
                int from = candidate.index() + 1;
                for (TimedCall call : following) {
                    int at = indexOfStop(candidate.trip().stops(), call.stopAreaId(), from);
                    Duration callGap = at < 0 ? null : gap(candidate.trip().stops().get(at), call, sameNumber);
                    if (callGap != null && callGap.compareTo(sameNumber ? NUMBER_TOLERANCE : SCHEDULE_TOLERANCE) <= 0) {
                        matched++;
                        from = at + 1;
                    }
                }
                Candidate scored = new Candidate(sameNumber, matched, gap);
                if (bestCandidate == null || order.compare(scored, bestCandidate) > 0) {
                    bestCandidate = scored;
                    best = candidate;
                }
            }
        }
        return bestCandidate != null && (bestCandidate.sameNumber() || bestCandidate.matched() >= following.size()) ? best : null;
    }

    private record Candidate(boolean sameNumber, int matched, Duration gap) {
    }

    /**
     * Between a scheduled stop and a call's scheduled time (its expected one for a train known by its number); null
     * without such a time
     */
    private static Duration gap(ScheduledStop stop, TimedCall call, boolean sameNumber) {
        Instant time = call.aimed() != null ? call.aimed() : sameNumber ? call.expected() : null;
        if (time == null) {
            return null;
        }
        Duration arrival = Duration.between(stop.arrival(), time).abs();
        Duration departure = Duration.between(stop.departure(), time).abs();
        return arrival.compareTo(departure) <= 0 ? arrival : departure;
    }

    private static int indexOfStop(List<ScheduledStop> stops, String stopAreaId, int from) {
        for (int i = from; i < stops.size(); i++) {
            if (stops.get(i).stopAreaId().equals(stopAreaId)) {
                return i;
            }
        }
        return -1;
    }

    private static int indexOf(List<StopRef> stops, String stopAreaId, int from) {
        for (int i = from; i < stops.size(); i++) {
            if (stops.get(i).id().equals(stopAreaId)) {
                return i;
            }
        }
        return -1;
    }

    /** Calls of the run at stops of the line */
    private static List<VehicleCall> calls(Run run, Map<String, String> names) {
        return run.calls().stream()
            .filter(call -> names.containsKey(call.stopAreaId()))
            .map(call -> call(call, names.get(call.stopAreaId())))
            .toList();
    }

    /**
     * @param left the stop it left, when known (null stop: its trip starts at the next stop)
     */
    private static Optional<Vehicle> toVehicle(Run run, LineDetail detail, Left left, List<VehicleCall> nextCalls, Instant now) {
        List<TimedCall> calls = run.calls();
        TimedCall next = calls.getFirst();
        TimedCall following = calls.size() > 1 ? calls.get(1) : null;
        Duration toNext = Duration.between(now, next.expected());

        if (left != null && left.stopAreaId() != null) {
            // Branch serving the stop left then the next stops: the one it comes from where branches merge
            List<String> stopIds = new ArrayList<>(List.of(left.stopAreaId()));
            stopIds.addAll(stopIds(run));
            Position position = locate(detail, stopIds);
            if (position != null) {
                LineBranch branch = detail.directions().get(position.direction()).branches().get(position.branch());
                int to = indexOf(branch.stops(), next.stopAreaId(), position.index() + 1);
                Duration segment = clamp(Duration.between(left.at(), next.expected()), MIN_SEGMENT, MAX_RIDE);
                if (to > position.index()) {
                    if (toNext.compareTo(segment.plus(HELD)) > 0) {
                        // Not gone from there yet, long after its departure time: data to wait for
                        return Optional.empty();
                    }
                    String id = Optional.ofNullable(run.id()).orElse(position.direction() + ":" + position.branch() + ":" + next.expected());
                    return Optional.of(vehicle(id, run, run.name(), branch, position.direction(), position.branch(), to,
                        branch.stops().get(position.index()), next, progress(toNext, segment), nextCalls));
                }
            }
        }

        // Branch serving the next stop then most of the following ones, in order (branches share their trunk)
        Position position = locate(detail, stopIds(run));
        if (position == null) {
            return Optional.empty();
        }
        LineBranch branch = detail.directions().get(position.direction()).branches().get(position.branch());
        boolean starts = left != null && left.stopAreaId() == null;
        StopRef from = position.index() > 0 && !starts ? branch.stops().get(position.index() - 1) : null;

        double progress;
        if (from == null) {
            // Waiting at the first stop of the branch, or of its trip
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
        return Optional.of(vehicle(id, run, run.name(), branch, position.direction(), position.branch(), position.index(), from, next,
            progress, nextCalls));
    }

    /** A passage at a stop of a branch */
    private record Passage(TimedCall call, Run run) {
    }

    /**
     * Vehicles from passages per stop, along each branch: each vehicle is followed from stop to stop (see
     * {@link #trace}); one first seen at a stop has left the previous one. Closed stops without passages are left
     * out: vehicles go through them.
     */
    private static List<Vehicle> fromStops(LineDetail detail, List<Run> runs, Set<String> closed, Instant now) {
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
                List<StopRef> branchStops = branch.stops();
                int last = branchStops.size() - 1;
                Set<String> served = new HashSet<>();
                Set<String> repeated = new HashSet<>();
                branchStops.forEach(stop -> {
                    if (!served.add(stop.id())) {
                        repeated.add(stop.id());
                    }
                });
                // Stops served now, by their index in the branch (below, i is a position in this list)
                List<Integer> open = new ArrayList<>();
                for (int i = 0; i < branchStops.size(); i++) {
                    String id = branchStops.get(i).id();
                    if (!closed.contains(id) || byStop.containsKey(id)) {
                        open.add(i);
                    }
                }
                List<StopRef> stops = open.stream().map(branchStops::get).toList();
                List<List<Passage>> passages = new ArrayList<>();
                for (int i = 0; i < stops.size(); i++) {
                    int index = open.get(i);
                    passages.add(repeated.contains(stops.get(i).id())
                        // Served several times (loop): its passages can't be told apart
                        ? List.of()
                        : byStop.getOrDefault(stops.get(i).id(), List.of()).stream()
                        .filter(passage -> {
                            // Going further on this branch (or ending here, but not at its first stop)
                            int to = destinationIndex(branch, passage.run(), index);
                            return to > index || (to == index && index > 0);
                        })
                        .sorted(Comparator.comparing(passage -> passage.call().expected()))
                        .collect(ArrayList::new, (list, passage) -> {
                            // The same vehicle listed by several quays with slightly different times
                            if (list.isEmpty() || Duration.between(list.getLast().call().expected(), passage.call().expected()).compareTo(SAME_VEHICLE) > 0) {
                                list.add(passage);
                            }
                        }, ArrayList::addAll));
                }
                List<Duration> segments = new ArrayList<>();
                for (int i = 0; i < stops.size(); i++) {
                    segments.add(i == 0 ? DEFAULT_SEGMENT : segment(passages.get(i - 1), passages.get(i)));
                }

                for (Trace trace : trace(passages, segments)) {
                    int i = trace.first();
                    Passage passage = trace.passages().getFirst();
                    List<VehicleCall> calls = new ArrayList<>();
                    for (int k = 0; k < trace.passages().size(); k++) {
                        calls.add(call(trace.passages().get(k).call(), stops.get(trace.stops().get(k)).name()));
                    }
                    Duration toNext = Duration.between(now, passage.call().expected());
                    Vehicle vehicle;
                    if (i == 0) {
                        // Waiting at the first stop
                        if (toNext.compareTo(TERMINUS_WINDOW) > 0) {
                            continue;
                        }
                        vehicle = vehicle(null, passage.run(), null, branch, d, b, open.get(0), null, passage.call(), 1, calls);
                    } else {
                        Duration segment = segments.get(i);
                        if (toNext.compareTo(segment.multipliedBy(2).plusMinutes(1)) > 0) {
                            // Seen from here on only: beyond what earlier stops list, or not coming from the previous stop
                            continue;
                        }
                        vehicle = vehicle(null, passage.run(), null, branch, d, b, open.get(i), stops.get(i - 1), passage.call(),
                            progress(toNext, segment), calls);
                    }
                    add(vehicles, onOwnBranch, vehicle, destinationIndex(branch, passage.run(), open.get(i)) == last);
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

    /**
     * A vehicle followed along a branch
     *
     * @param stops    indexes of the stops where it is seen; it is between the stop before the first one and that one
     * @param passages its passages there
     */
    private record Trace(List<Integer> stops, List<Passage> passages) {

        int first() {
            return stops.getFirst();
        }

        int last() {
            return stops.getLast();
        }

        Instant time() {
            return passages.getLast().call().expected();
        }
    }

    /**
     * Vehicles followed stop by stop: a passage continues the earliest vehicle seen at one of the two previous stops
     * that can get there by then (vehicles don't overtake, and a stop sometimes misses a vehicle for a while), or
     * is a vehicle first seen there, i.e. that has left the previous stop.
     */
    private static List<Trace> trace(List<List<Passage>> passages, List<Duration> segments) {
        List<Trace> traces = new ArrayList<>();
        for (int i = 0; i < passages.size(); i++) {
            int index = i;
            List<Integer> candidates = new ArrayList<>();
            for (int t = 0; t < traces.size(); t++) {
                if (traces.get(t).last() < i && traces.get(t).last() >= i - 2) {
                    candidates.add(t);
                }
            }
            candidates.sort(Comparator.comparing(t -> traces.get(t).time()));
            for (Passage passage : passages.get(i)) {
                Instant expected = passage.call().expected();
                Integer match = candidates.stream().filter(t -> {
                    Trace trace = traces.get(t);
                    Duration bound = Duration.ZERO;
                    for (int k = trace.last() + 1; k <= index; k++) {
                        bound = bound.plus(segments.get(k).multipliedBy(2)).plusMinutes(1);
                    }
                    return expected.isAfter(trace.time().minus(SAME_VEHICLE)) && !expected.isAfter(trace.time().plus(bound));
                }).findFirst().orElse(null);
                if (match == null) {
                    traces.add(new Trace(new ArrayList<>(List.of(i)), new ArrayList<>(List.of(passage))));
                } else {
                    candidates.remove(match);
                    traces.get(match).stops().add(i);
                    traces.get(match).passages().add(passage);
                }
            }
        }
        return traces;
    }

    /** Usual time from a passage at the previous stop to the next one at this stop (median) */
    private static Duration segment(List<Passage> before, List<Passage> here) {
        List<Duration> gaps = new ArrayList<>();
        for (Passage previous : before) {
            Instant left = previous.call().expected();
            here.stream()
                .map(passage -> Duration.between(left, passage.call().expected()))
                .filter(gap -> gap.isPositive() && gap.compareTo(MAX_SEGMENT) <= 0)
                .findFirst()
                .ifPresent(gaps::add);
        }
        if (gaps.isEmpty()) {
            return DEFAULT_SEGMENT;
        }
        gaps.sort(null);
        return clamp(gaps.get(gaps.size() / 2), MIN_SEGMENT, MAX_SEGMENT);
    }

    private static double progress(Duration toNext, Duration segment) {
        return Math.clamp(1 - (double) toNext.toSeconds() / segment.toSeconds(), 0, 1);
    }

    /**
     * @param id    journey id, null for a vehicle found from passages per stop (an id is made from where it is)
     * @param name  mission code or train number
     * @param index position of the next stop in the branch
     * @param calls its next stops, from the next one
     */
    private static Vehicle vehicle(String id, Run run, String name, LineBranch branch, int direction, int branchIndex, int index,
                                   StopRef from, TimedCall next, double progress, List<VehicleCall> calls) {
        StopRef to = branch.stops().get(index);
        int destination = destinationIndex(branch, run, index);
        if (id == null) {
            id = "%d:%s:%s".formatted(direction, to.id(), next.expected().getEpochSecond());
        }
        // The announced destination when it is a stop ahead on the branch (it is sometimes the other end of the line),
        // the branch's terminus otherwise
        String headsign = destination >= 0 && run.destination() != null ? run.destination() : branch.stops().getLast().name();
        return new Vehicle(id, name, headsign, direction, branchIndex, from == null ? null : from.id(), to.id(), to.name(), progress,
            next.expected(), delay(next), calls);
    }

    private static VehicleCall call(TimedCall call, String stopName) {
        return new VehicleCall(call.stopAreaId(), stopName, call.expected(), delay(call), call.platform());
    }

    private static Integer delay(TimedCall call) {
        return call.aimed() == null ? null : (int) Duration.between(call.aimed(), call.expected()).toSeconds();
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

    private static List<RecordedCall> recordedCalls(EstimatedVehicleJourney journey) {
        return Optional.ofNullable(journey.getRecordedCalls()).map(RecordedCalls::getRecordedCall).orElse(List.of());
    }

    private static String quayId(EstimatedCall call) {
        return gtfsId(Optional.ofNullable(call.getStopPointRef()).map(StopPointRef::getValue).orElse(null));
    }

    private static String quayId(RecordedCall call) {
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
