package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.Departure;
import com.gdamiens.website.controller.object.v2.LineBranch;
import com.gdamiens.website.controller.object.v2.LineDepartures;
import com.gdamiens.website.controller.object.v2.LineDetail;
import com.gdamiens.website.controller.object.v2.Ride;
import com.gdamiens.website.controller.object.v2.StopDepartures;
import com.gdamiens.website.controller.object.v2.StopRef;
import com.gdamiens.website.controller.object.v2.VehicleCall;
import com.gdamiens.website.model.IDFMRoute;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.repository.NetworkRepository.ScheduledRideRow;
import com.gdamiens.website.service.VehicleService.Trip;
import com.gdamiens.website.utils.TtlCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Next departures of a line from a stop area that stop at a further one, with their arrival there: what a traveller
 * can take for a ride of a journey. Cached 30 s per line and stop pair.
 * <p>
 * The arrival comes from the vehicle's real-time calls (estimated-timetable trips of {@link VehicleService}), else
 * from its scheduled GTFS trip shifted by its delay, else from the line's usual ride time between the two stops.
 */
@Service
public class RideService {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    public static final int MAX_RIDES = 10;

    /** Same vehicle in the stop's departures and the line's real-time trips */
    private static final Duration SAME_PASSAGE = Duration.ofSeconds(90);

    /** A list starting this soon starts now */
    private static final Duration NEAR_FUTURE = Duration.ofMinutes(5);

    /** A list starting at a given time keeps the departures this much before */
    private static final Duration PAST_MARGIN = Duration.ofMinutes(10);

    /** Same trip in the real-time departures and the schedule */
    private static final Duration SAME_SCHEDULE = Duration.ofSeconds(60);

    /** Scheduled trips used for the usual ride time: leaving this close to the departure */
    private static final Duration TYPICAL_WINDOW = Duration.ofMinutes(45);

    /** Words left out when comparing destination names ("Paris Saint-Lazare" / "Gare Saint-Lazare") */
    private static final Pattern GENERIC_WORDS = Pattern.compile("\\b(gare|paris|de|des|du|la|le|les|l|d|station)\\b");

    private final DepartureService departureService;

    private final VehicleService vehicleService;

    private final NetworkService networkService;

    private final NetworkRepository networkRepository;

    private final TtlCache<Key, List<Ride>> cache = new TtlCache<>(Duration.ofSeconds(30), 5000);

    public RideService(DepartureService departureService, VehicleService vehicleService, NetworkService networkService,
                       NetworkRepository networkRepository) {
        this.departureService = departureService;
        this.vehicleService = vehicleService;
        this.networkService = networkService;
        this.networkRepository = networkRepository;
    }

    /**
     * @param after start of the list (to the minute), null for now
     */
    private record Key(String lineId, String fromId, String toId, Instant after) {
    }

    /**
     * @param after departures from then on (e.g. arrival at a connection), now when null or in the past
     * @param limit rides returned, at most {@link #MAX_RIDES}
     * @return departures of the line at [fromId] stopping at [toId], earliest first
     */
    public List<Ride> getRides(String lineId, String fromId, String toId, Instant after, int limit) {
        Instant now = Instant.now();
        // Close to now, the list from now is shared
        Instant start = after == null || after.isBefore(now.plus(NEAR_FUTURE)) ? null
            : after.truncatedTo(ChronoUnit.MINUTES);
        return cache.get(new Key(lineId, fromId, toId, start), this::load).stream()
            .filter(ride -> start == null || !ride.departure().time().isBefore(start.minus(PAST_MARGIN)))
            .limit(Math.clamp(limit, 1, MAX_RIDES))
            .toList();
    }

    private List<Ride> load(Key key) {
        List<LineDepartures> groups = departureService.getDepartures(key.fromId(), key.lineId(), DepartureService.MAX_DEPARTURES)
            .map(StopDepartures::lines).orElse(List.of());

        Instant now = Instant.now();
        ZonedDateTime base = Optional.ofNullable(key.after()).orElse(now).atZone(PARIS);
        int baseSeconds = base.toLocalTime().toSecondOfDay();
        List<Scheduled> scheduled = networkRepository.findScheduledRides(IDFMRoute.toRouteId(key.lineId()), key.fromId(), key.toId(),
                base.toLocalDate(), baseSeconds - 3600, baseSeconds + 3 * 3600).stream()
            .map(row -> new Scheduled(row.headsign(), DepartureService.isTrainNumber(row.tripShortName()) ? row.tripShortName() : null, row.terminus(), toInstant(row.serviceDate(), row.departureSeconds()),
                toInstant(row.serviceDate(), row.arrivalSeconds())))
            .toList();
        boolean realtime = groups.stream().anyMatch(group -> group.departures().stream().anyMatch(Departure::realtime));
        List<Trip> trips = realtime ? vehicleService.getTrips(key.lineId()) : List.of();
        Set<String> destinations = destinations(key, scheduled);

        List<Ride> rides = new ArrayList<>();
        // Departures listed by the stop for the destinations going to [to], until when each destination is listed
        List<Departure> listed = new ArrayList<>();
        Instant covered = null;
        for (LineDepartures group : groups) {
            boolean towards = served(group.destination(), destinations);
            int before = rides.size();
            for (Departure departure : group.departures()) {
                ride(key, group.destination(), departure, towards, trips, scheduled).ifPresent(rides::add);
            }
            if (rides.size() > before) {
                listed.addAll(group.departures());
                Instant last = group.departures().getLast().time();
                covered = covered == null || last.isBefore(covered) ? last : covered;
            }
        }

        // Beyond what the stop lists (real time only goes so far): the schedule, without the trips already listed
        Instant until = Optional.ofNullable(covered).orElse(now);
        for (Scheduled ride : scheduled) {
            boolean known = listed.stream().anyMatch(departure -> ride.number() != null
                ? ride.number().equals(departure.trainNumber())
                : departure.aimedTime() != null && Duration.between(departure.aimedTime(), ride.departure()).abs().compareTo(SAME_SCHEDULE) <= 0);
            if (!known && ride.departure().isAfter(until)) {
                String mission = DepartureService.isMission(ride.headsign()) ? ride.headsign() : null;
                Departure departure = new Departure(ride.departure(), ride.departure(), false, null, null, null, mission, ride.number());
                rides.add(new Ride(departure, Optional.ofNullable(ride.terminus()).orElse(ride.headsign()), ride.arrival(), "scheduled"));
            }
        }
        rides.sort(Comparator.comparing(ride -> ride.departure().time()));
        return rides;
    }

    /** A scheduled trip from [from] to [to] */
    private record Scheduled(String headsign, String number, String terminus, Instant departure, Instant arrival) {

        Duration duration() {
            return Duration.between(departure, arrival);
        }
    }

    /**
     * @param towards the destination is beyond [to] on the line
     * @return the departure when it stops at [to]
     */
    private static Optional<Ride> ride(Key key, String destination, Departure departure, boolean towards, List<Trip> trips,
                                       List<Scheduled> scheduled) {
        String number = departure.trainNumber();
        Instant aimed = Optional.ofNullable(departure.aimedTime()).orElse(departure.time());

        // Its real-time trip: same train number, else calling at [from] at the same time towards the same destination
        Trip trip = null;
        boolean skips = false;
        Duration best = SAME_PASSAGE;
        for (Trip candidate : trips) {
            boolean sameNumber = number != null && number.equals(candidate.name());
            if (!sameNumber && (candidate.destination() != null && !sameName(candidate.destination(), destination))) {
                continue;
            }
            Optional<VehicleCall> call = candidate.calls().stream().filter(c -> c.stopId().equals(key.fromId())).findFirst();
            Duration gap = call.map(c -> Duration.between(c.expectedAt(), departure.time()).abs()).orElse(null);
            if (gap == null || (!sameNumber && gap.compareTo(SAME_PASSAGE) > 0)) {
                continue;
            }
            if (arrival(candidate, key) == null) {
                // A trip whose calls go to its destination doesn't stop there
                skips |= candidate.complete() && (sameNumber || number == null);
            } else if (sameNumber || gap.compareTo(best) <= 0) {
                best = sameNumber ? Duration.ZERO : gap;
                trip = candidate;
            }
        }
        VehicleCall arrival = trip == null ? null : arrival(trip, key);
        if (arrival == null && skips) {
            return Optional.empty();
        }

        // Its scheduled trip: same train number, else leaving at the same scheduled time towards the same destination
        // (none for real-time departures without scheduled time: metros, whose GTFS trips are only frequencies)
        boolean numbered = number != null && scheduled.stream().anyMatch(ride -> ride.number() != null);
        Scheduled same = scheduled.stream()
            .filter(ride -> numbered
                ? number.equals(ride.number())
                : departure.aimedTime() != null && Duration.between(ride.departure(), aimed).abs().compareTo(SAME_SCHEDULE) <= 0
                    && (towards || sameName(ride.headsign(), destination)))
            .min(Comparator.comparing(ride -> Duration.between(ride.departure(), aimed).abs()))
            .orElse(null);
        if (arrival == null && same == null && (numbered || !towards)) {
            // Not stopping at [to], or not going that way
            return Optional.empty();
        }

        String mission = Stream.of(departure.mission(), same == null ? null : same.headsign(), trip == null ? null : trip.name())
            .filter(DepartureService::isMission).findFirst().orElse(null);
        Departure shown = Objects.equals(mission, departure.mission()) ? departure : new Departure(departure.time(), departure.aimedTime(),
            departure.realtime(), departure.status(), departure.platform(), departure.atStop(), mission, number);
        if (arrival != null) {
            return Optional.of(new Ride(shown, destination, arrival.expectedAt(), "realtime"));
        }
        if (same != null) {
            Duration delay = Duration.between(aimed, departure.time());
            return Optional.of(new Ride(shown, destination, same.arrival().plus(delay), "scheduled"));
        }

        // Usual ride time around then
        List<Duration> durations = scheduled.stream()
            .filter(ride -> Duration.between(ride.departure(), departure.time()).abs().compareTo(TYPICAL_WINDOW) <= 0)
            .map(Scheduled::duration)
            .sorted()
            .toList();
        Instant arrivalAt = durations.isEmpty() ? null : departure.time().plus(durations.get(durations.size() / 2));
        return Optional.of(new Ride(shown, destination, arrivalAt, arrivalAt == null ? null : "typical"));
    }

    /** Call of the trip at [to] after its call at [from] */
    private static VehicleCall arrival(Trip trip, Key key) {
        List<VehicleCall> calls = trip.calls();
        for (int i = 0; i < calls.size(); i++) {
            if (calls.get(i).stopId().equals(key.fromId())) {
                return calls.subList(i + 1, calls.size()).stream()
                    .filter(call -> call.stopId().equals(key.toId())).findFirst().orElse(null);
            }
        }
        return null;
    }

    /**
     * Destinations of trips from [from] stopping at [to]: headsigns of the scheduled ones, and stops at or after
     * [to] on the branches serving [from] then [to]
     */
    private Set<String> destinations(Key key, List<Scheduled> scheduled) {
        Set<String> names = new HashSet<>();
        scheduled.forEach(ride -> names.add(ride.headsign()));
        LineDetail detail = networkService.getLineDetail(key.lineId()).orElse(null);
        if (detail != null) {
            for (var direction : detail.directions()) {
                for (LineBranch branch : direction.branches()) {
                    List<String> ids = branch.stops().stream().map(StopRef::id).toList();
                    int from = ids.indexOf(key.fromId());
                    int to = ids.lastIndexOf(key.toId());
                    if (from >= 0 && to > from) {
                        branch.stops().subList(to, ids.size()).forEach(stop -> names.add(stop.name()));
                    }
                }
            }
        }
        names.remove(null);
        return names;
    }

    private static boolean served(String destination, Set<String> destinations) {
        return destinations.stream().anyMatch(name -> sameName(name, destination));
    }

    /** Same place, one name containing the other, generic words aside */
    static boolean sameName(String a, String b) {
        String x = normalize(a);
        String y = normalize(b);
        if (x.isEmpty() || y.isEmpty()) {
            return false;
        }
        if (x.contains(y) || y.contains(x)) {
            return true;
        }
        String u = GENERIC_WORDS.matcher(x).replaceAll(" ").replaceAll(" +", " ").trim();
        String v = GENERIC_WORDS.matcher(y).replaceAll(" ").replaceAll(" +", " ").trim();
        return !u.isEmpty() && !v.isEmpty() && (u.contains(v) || v.contains(u));
    }

    private static String normalize(String name) {
        return StringUtils.stripAccents(StringUtils.defaultString(name)).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    /** GTFS times are counted from noon minus 12 h of the service day, which matters on DST change days */
    private static Instant toInstant(LocalDate serviceDate, int seconds) {
        return ZonedDateTime.of(serviceDate, LocalTime.NOON, PARIS).minusHours(12).plusSeconds(seconds).toInstant();
    }
}
