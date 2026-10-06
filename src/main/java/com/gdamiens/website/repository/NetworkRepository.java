package com.gdamiens.website.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Read-only queries on the {@code gtfs} schema for the mobility (v2) endpoints. Stop areas are GTFS parent stations
 * ({@code location_type = 1}); every quay ({@code location_type = 0}) has one.
 */
@Repository
public class NetworkRepository {

    public record RouteRow(String routeId, String shortName, String longName, Short type, String color, String textColor, String agencyName) {}

    public record StopAreaRow(String id, String name, double lat, double lon, Integer distance) {}

    public record QuayRow(String id, String name, double lat, double lon, String platformCode, Short wheelchairBoarding, List<String> routeIds) {}

    public record ConnectionRow(String id, String name, Integer minTransferSeconds) {}

    public record TripPatternRow(int directionId, List<String> stopAreaIds, int tripCount, String shapeId, String headsign) {}

    public record ScheduledDepartureRow(String routeId, String headsign, LocalDate serviceDate, int departureSeconds) {}

    public record TimetableRow(Short directionId, int departureSeconds, String headsign, String terminusId, String terminus) {}

    public record ScheduledRideRow(String headsign, String tripShortName, String terminus, LocalDate serviceDate, int departureSeconds, int arrivalSeconds) {}

    private static final String STOP_AREA_COLUMNS = "p.stop_id, p.stop_name, p.stop_lat, p.stop_lon";

    private final NamedParameterJdbcTemplate jdbc;

    public NetworkRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<RouteRow> findAllRoutes() {
        return jdbc.query("""
                SELECT r.route_id, r.route_short_name, r.route_long_name, r.route_type, r.route_color, r.route_text_color, a.agency_name
                FROM gtfs.routes r LEFT JOIN gtfs.agency a ON a.agency_id = r.agency_id""",
            (rs, i) -> new RouteRow(rs.getString(1), rs.getString(2), rs.getString(3), getShort(rs, 4),
                rs.getString(5), rs.getString(6), rs.getString(7)));
    }

    /**
     * Stop areas within {@code radius} meters, closest first. The GiST index gives the closest candidates, then the
     * exact distance filters them.
     */
    public List<StopAreaRow> findNearbyStopAreas(double lat, double lon, int radius, int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("lat", lat).addValue("lon", lon).addValue("radius", radius)
            .addValue("limit", limit).addValue("candidates", Math.max(limit * 4, 50));

        return jdbc.query("""
                SELECT * FROM (
                  SELECT %s, round(ST_Distance(p.geom::geography, pt.geom::geography))::int AS distance
                  FROM gtfs.stops p, (SELECT ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geom) pt
                  WHERE p.location_type = 1
                  ORDER BY p.geom <-> pt.geom
                  LIMIT :candidates
                ) x
                WHERE x.distance <= :radius
                ORDER BY x.distance
                LIMIT :limit""".formatted(STOP_AREA_COLUMNS),
            params,
            (rs, i) -> new StopAreaRow(rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4), rs.getInt(5)));
    }

    public Optional<StopAreaRow> findStopArea(String id) {
        return jdbc.query("SELECT %s FROM gtfs.stops p WHERE p.stop_id = :id AND p.location_type = 1".formatted(STOP_AREA_COLUMNS),
                Map.of("id", id),
                (rs, i) -> new StopAreaRow(rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4), null))
            .stream().findFirst();
    }

    /**
     * @return route ids serving each stop area
     */
    public Map<String, List<String>> findRouteIdsByStopAreas(Collection<String> stopAreaIds) {
        Map<String, List<String>> result = new HashMap<>();
        if (stopAreaIds.isEmpty()) {
            return result;
        }

        jdbc.query("""
                SELECT q.parent_station, array_agg(DISTINCT rs.route_id)
                FROM gtfs.stops q JOIN gtfs.route_stops rs ON rs.stop_id = q.stop_id
                WHERE q.parent_station IN (:ids)
                GROUP BY q.parent_station""",
            Map.of("ids", stopAreaIds),
            rs -> {
                result.put(rs.getString(1), toList(rs.getArray(2)));
            });
        return result;
    }

    /**
     * @return stop area (parent station) of each quay
     */
    public Map<String, String> findStopAreasOfQuays(Collection<String> quayIds) {
        if (quayIds.isEmpty()) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        jdbc.query("SELECT stop_id, parent_station FROM gtfs.stops WHERE stop_id IN (:ids) AND parent_station IS NOT NULL",
            Map.of("ids", quayIds),
            rs -> {
                result.put(rs.getString(1), rs.getString(2));
            });
        return result;
    }

    public List<QuayRow> findQuays(String stopAreaId) {
        return jdbc.query("""
                SELECT q.stop_id, q.stop_name, q.stop_lat, q.stop_lon, q.platform_code, q.wheelchair_boarding,
                       array_remove(array_agg(DISTINCT rs.route_id), NULL)
                FROM gtfs.stops q LEFT JOIN gtfs.route_stops rs ON rs.stop_id = q.stop_id
                WHERE q.parent_station = :id AND q.location_type = 0
                GROUP BY q.stop_id
                ORDER BY q.stop_name, q.platform_code, q.stop_id""",
            Map.of("id", stopAreaId),
            (rs, i) -> new QuayRow(rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4), rs.getString(5),
                getShort(rs, 6), toList(rs.getArray(7))));
    }

    /**
     * Other stop areas linked to this one by a GTFS transfer between their quays.
     */
    public List<ConnectionRow> findConnections(String stopAreaId) {
        return jdbc.query("""
                SELECT tp.stop_id, tp.stop_name, min(t.min_transfer_time)
                FROM gtfs.stops fq
                JOIN gtfs.transfers t ON t.from_stop_id = fq.stop_id
                JOIN gtfs.stops tq ON tq.stop_id = t.to_stop_id
                JOIN gtfs.stops tp ON tp.stop_id = tq.parent_station
                WHERE fq.parent_station = :id AND tp.stop_id <> :id
                GROUP BY tp.stop_id, tp.stop_name
                ORDER BY 3 NULLS LAST, 2""",
            Map.of("id", stopAreaId),
            (rs, i) -> new ConnectionRow(rs.getString(1), rs.getString(2), (Integer) rs.getObject(3)));
    }

    /**
     * Distinct stop-area sequences followed by the route's trips, with how many trips follow each one.
     */
    public List<TripPatternRow> findTripPatterns(String routeId) {
        return jdbc.query("""
                WITH seq AS (
                  SELECT t.trip_id, t.direction_id, t.shape_id, t.trip_headsign,
                         array_agg(q.parent_station ORDER BY st.stop_sequence) AS stops
                  FROM gtfs.trips t
                  JOIN gtfs.stop_times st ON st.trip_id = t.trip_id
                  JOIN gtfs.stops q ON q.stop_id = st.stop_id
                  WHERE t.route_id = :routeId
                  GROUP BY t.trip_id, t.direction_id, t.shape_id, t.trip_headsign
                )
                SELECT coalesce(direction_id, 0), stops, count(*)::int,
                       mode() WITHIN GROUP (ORDER BY shape_id), mode() WITHIN GROUP (ORDER BY trip_headsign)
                FROM seq
                GROUP BY coalesce(direction_id, 0), stops""",
            Map.of("routeId", routeId),
            (rs, i) -> new TripPatternRow(rs.getInt(1), toList(rs.getArray(2)), rs.getInt(3), rs.getString(4), rs.getString(5)));
    }

    public Map<String, StopAreaRow> findStopAreas(Collection<String> ids) {
        Map<String, StopAreaRow> result = new HashMap<>();
        if (ids.isEmpty()) {
            return result;
        }

        jdbc.query("SELECT %s FROM gtfs.stops p WHERE p.stop_id IN (:ids)".formatted(STOP_AREA_COLUMNS),
            Map.of("ids", ids),
            rs -> {
                result.put(rs.getString(1), new StopAreaRow(rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4), null));
            });
        return result;
    }

    /**
     * @return GeoJSON LineString of each shape, slightly simplified (about 2 m)
     */
    public Map<String, String> findShapesAsGeoJson(Collection<String> shapeIds) {
        Map<String, String> result = new HashMap<>();
        if (shapeIds.isEmpty()) {
            return result;
        }

        jdbc.query("SELECT shape_id, ST_AsGeoJSON(ST_Simplify(geom, 0.00002), 6) FROM gtfs.shapes WHERE shape_id IN (:ids)",
            Map.of("ids", shapeIds),
            rs -> {
                result.put(rs.getString(1), rs.getString(2));
            });
        return result;
    }

    /**
     * Path of a route between two points, cut from the GTFS shape of the route that passes closest to both, in this
     * order (each point within 300 m of the shape).
     *
     * @return GeoJSON LineString, empty if no shape of the route fits
     */
    public Optional<String> findShapeBetween(String routeId, double fromLat, double fromLon, double toLat, double toLon) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("routeId", routeId)
            .addValue("fromLat", fromLat).addValue("fromLon", fromLon)
            .addValue("toLat", toLat).addValue("toLon", toLon);

        return jdbc.query("""
                WITH pts AS (
                  SELECT ST_SetSRID(ST_MakePoint(:fromLon, :fromLat), 4326) AS a, ST_SetSRID(ST_MakePoint(:toLon, :toLat), 4326) AS b
                ),
                route_shapes AS (
                  SELECT DISTINCT shape_id FROM gtfs.trips WHERE route_id = :routeId AND shape_id IS NOT NULL
                ),
                candidates AS (
                  SELECT s.geom,
                         ST_LineLocatePoint(s.geom, pts.a) AS from_fraction,
                         ST_LineLocatePoint(s.geom, pts.b) AS to_fraction,
                         ST_Distance(s.geom::geography, pts.a::geography) + ST_Distance(s.geom::geography, pts.b::geography) AS gap,
                         ST_DWithin(s.geom::geography, pts.a::geography, 300) AND ST_DWithin(s.geom::geography, pts.b::geography, 300) AS close
                  FROM route_shapes JOIN gtfs.shapes s USING (shape_id), pts
                )
                SELECT ST_AsGeoJSON(ST_LineSubstring(geom, from_fraction, to_fraction), 6)
                FROM candidates
                WHERE close AND from_fraction < to_fraction
                ORDER BY gap
                LIMIT 1""",
            params,
            (rs, i) -> rs.getString(1)).stream().findFirst();
    }

    /**
     * Scheduled departures from the stop area's quays between {@code fromSeconds} and {@code toSeconds} after midnight
     * of {@code today} (Europe/Paris). Trips of the previous service day running after midnight are included.
     * Quays are read first so that {@code stop_times} is scanned through its {@code (stop_id, departure_time)} index.
     */
    public List<ScheduledDepartureRow> findScheduledDepartures(String stopAreaId, LocalDate today, int fromSeconds, int toSeconds) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", stopAreaId).addValue("today", today)
            .addValue("from", fromSeconds).addValue("to", toSeconds);

        return jdbc.query("""
                WITH days AS (
                  SELECT CAST(:today AS date) AS service_date, make_interval(secs => :from) AS lo, make_interval(secs => :to) AS hi
                  UNION ALL
                  SELECT CAST(:today AS date) - 1, make_interval(secs => :from + 86400), make_interval(secs => :to + 86400)
                ),
                active AS (
                  SELECT d.service_date, c.service_id
                  FROM days d JOIN gtfs.calendar c ON d.service_date BETWEEN c.start_date AND c.end_date
                   AND (ARRAY[c.monday, c.tuesday, c.wednesday, c.thursday, c.friday, c.saturday, c.sunday])[extract(isodow FROM d.service_date)::int]
                  UNION
                  SELECT cd.date, cd.service_id FROM gtfs.calendar_dates cd JOIN days d ON d.service_date = cd.date WHERE cd.exception_type = 1
                  EXCEPT
                  SELECT cd.date, cd.service_id FROM gtfs.calendar_dates cd JOIN days d ON d.service_date = cd.date WHERE cd.exception_type = 2
                ),
                quays AS MATERIALIZED (
                  SELECT stop_id FROM gtfs.stops WHERE parent_station = :id
                ),
                candidates AS MATERIALIZED (
                  SELECT d.service_date, st.trip_id, st.departure_time, st.stop_headsign
                  FROM days d CROSS JOIN quays qq
                  JOIN LATERAL (
                    SELECT s.trip_id, s.departure_time, s.stop_headsign FROM gtfs.stop_times s
                    WHERE s.stop_id = qq.stop_id AND s.departure_time BETWEEN d.lo AND d.hi AND coalesce(s.pickup_type, 0) <> 1
                  ) st ON true
                )
                SELECT t.route_id, coalesce(c.stop_headsign, t.trip_headsign), c.service_date, extract(epoch FROM c.departure_time)::int
                FROM candidates c
                JOIN gtfs.trips t ON t.trip_id = c.trip_id
                JOIN active a ON a.service_id = t.service_id AND a.service_date = c.service_date
                ORDER BY c.service_date + c.departure_time""",
            params,
            (rs, i) -> new ScheduledDepartureRow(rs.getString(1), rs.getString(2), rs.getObject(3, LocalDate.class), rs.getInt(4)));
    }

    /**
     * Scheduled trips of the route leaving a quay of {@code fromId} between {@code fromSeconds} and {@code toSeconds}
     * after midnight of {@code today} (Europe/Paris, trips of the previous service day included) and stopping
     * further at a quay of {@code toId}, with the times at both and the name of the trip's last stop.
     */
    public List<ScheduledRideRow> findScheduledRides(String routeId, String fromId, String toId, LocalDate today, int fromSeconds, int toSeconds) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("routeId", routeId).addValue("fromId", fromId).addValue("toId", toId).addValue("today", today)
            .addValue("from", fromSeconds).addValue("to", toSeconds);

        return jdbc.query("""
                WITH days AS (
                  SELECT CAST(:today AS date) AS service_date, make_interval(secs => :from) AS lo, make_interval(secs => :to) AS hi
                  UNION ALL
                  SELECT CAST(:today AS date) - 1, make_interval(secs => :from + 86400), make_interval(secs => :to + 86400)
                ),
                active AS (
                  SELECT d.service_date, c.service_id
                  FROM days d JOIN gtfs.calendar c ON d.service_date BETWEEN c.start_date AND c.end_date
                   AND (ARRAY[c.monday, c.tuesday, c.wednesday, c.thursday, c.friday, c.saturday, c.sunday])[extract(isodow FROM d.service_date)::int]
                  UNION
                  SELECT cd.date, cd.service_id FROM gtfs.calendar_dates cd JOIN days d ON d.service_date = cd.date WHERE cd.exception_type = 1
                  EXCEPT
                  SELECT cd.date, cd.service_id FROM gtfs.calendar_dates cd JOIN days d ON d.service_date = cd.date WHERE cd.exception_type = 2
                ),
                from_quays AS MATERIALIZED (
                  SELECT stop_id FROM gtfs.stops WHERE parent_station = :fromId
                ),
                to_quays AS MATERIALIZED (
                  SELECT stop_id FROM gtfs.stops WHERE parent_station = :toId
                ),
                candidates AS MATERIALIZED (
                  SELECT d.service_date, st.trip_id, st.departure_time, st.stop_sequence, st.stop_headsign
                  FROM days d CROSS JOIN from_quays fq
                  JOIN LATERAL (
                    SELECT s.trip_id, s.departure_time, s.stop_sequence, s.stop_headsign FROM gtfs.stop_times s
                    WHERE s.stop_id = fq.stop_id AND s.departure_time BETWEEN d.lo AND d.hi AND coalesce(s.pickup_type, 0) <> 1
                  ) st ON true
                )
                SELECT DISTINCT coalesce(c.stop_headsign, t.trip_headsign), t.trip_short_name, last.name, c.service_date,
                       extract(epoch FROM c.departure_time)::int, extract(epoch FROM a.arrival_time)::int
                FROM candidates c
                JOIN gtfs.trips t ON t.trip_id = c.trip_id AND t.route_id = :routeId
                JOIN active ac ON ac.service_id = t.service_id AND ac.service_date = c.service_date
                JOIN gtfs.stop_times a ON a.trip_id = c.trip_id AND a.stop_sequence > c.stop_sequence
                 AND a.stop_id IN (SELECT stop_id FROM to_quays) AND coalesce(a.drop_off_type, 0) <> 1
                CROSS JOIN LATERAL (
                  SELECT coalesce(p.stop_name, q.stop_name) AS name
                  FROM gtfs.stop_times l JOIN gtfs.stops q ON q.stop_id = l.stop_id LEFT JOIN gtfs.stops p ON p.stop_id = q.parent_station
                  WHERE l.trip_id = c.trip_id ORDER BY l.stop_sequence DESC LIMIT 1
                ) last
                ORDER BY 4, 5""",
            params,
            (rs, i) -> new ScheduledRideRow(rs.getString(1), rs.getString(2), rs.getString(3), rs.getObject(4, LocalDate.class), rs.getInt(5), rs.getInt(6)));
    }

    /**
     * Scheduled departures of a route from the stop area's quays over the service day {@code date} (times may go past
     * 24:00), with the trip's direction, headsign and last stop
     */
    public List<TimetableRow> findTimetable(String stopAreaId, String routeId, LocalDate date) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", stopAreaId).addValue("routeId", routeId).addValue("date", date);

        return jdbc.query("""
                WITH active AS (
                  SELECT c.service_id FROM gtfs.calendar c
                  WHERE CAST(:date AS date) BETWEEN c.start_date AND c.end_date
                    AND (ARRAY[c.monday, c.tuesday, c.wednesday, c.thursday, c.friday, c.saturday, c.sunday])[extract(isodow FROM CAST(:date AS date))::int]
                  UNION
                  SELECT service_id FROM gtfs.calendar_dates WHERE date = :date AND exception_type = 1
                  EXCEPT
                  SELECT service_id FROM gtfs.calendar_dates WHERE date = :date AND exception_type = 2
                ),
                trips AS MATERIALIZED (
                  SELECT t.trip_id, t.direction_id, t.trip_headsign FROM gtfs.trips t JOIN active a ON a.service_id = t.service_id
                  WHERE t.route_id = :routeId
                )
                SELECT DISTINCT t.direction_id, extract(epoch FROM st.departure_time)::int, coalesce(st.stop_headsign, t.trip_headsign),
                       last.id, last.name
                FROM trips t
                JOIN gtfs.stop_times st ON st.trip_id = t.trip_id AND coalesce(st.pickup_type, 0) <> 1
                JOIN gtfs.stops q ON q.stop_id = st.stop_id AND q.parent_station = :id
                CROSS JOIN LATERAL (
                  SELECT coalesce(p.stop_id, lq.stop_id) AS id, coalesce(p.stop_name, lq.stop_name) AS name
                  FROM gtfs.stop_times l JOIN gtfs.stops lq ON lq.stop_id = l.stop_id LEFT JOIN gtfs.stops p ON p.stop_id = lq.parent_station
                  WHERE l.trip_id = t.trip_id ORDER BY l.stop_sequence DESC LIMIT 1
                ) last
                ORDER BY 2""",
            params,
            (rs, i) -> new TimetableRow(getShort(rs, 1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5)));
    }

    private static Short getShort(ResultSet rs, int column) throws SQLException {
        short value = rs.getShort(column);
        return rs.wasNull() ? null : value;
    }

    private static List<String> toList(Array array) throws SQLException {
        return array == null ? List.of() : Arrays.asList((String[]) array.getArray());
    }
}
