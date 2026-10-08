package com.gdamiens.website.service;

import com.gdamiens.website.configuration.HttpClientConfig;
import com.gdamiens.website.controller.object.v2.BikeStation;
import com.gdamiens.website.utils.Constants;
import org.apache.hc.client5.http.classic.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vélib stations from the GBFS open data (no key): locations refreshed every 6 h, availability every minute. One
 * fetch at a time; a failed fetch keeps the last data.
 */
@Service
public class BikeService {

    private static final Logger log = LoggerFactory.getLogger(BikeService.class);

    private static final Duration INFORMATION_TTL = Duration.ofHours(6);

    private static final Duration STATUS_TTL = Duration.ofSeconds(60);

    /** Stations returned for an area at most */
    public static final int MAX_STATIONS = 300;

    private final RestTemplate restTemplate;

    private record Information(String id, String code, String name, double lat, double lon, int capacity) {
    }

    private record Status(int mechanical, int electric, int docks, boolean renting, boolean returning, Instant reportedAt) {
    }

    private volatile Map<String, Information> information = Map.of();
    private volatile Instant informationAt = Instant.EPOCH;
    private volatile Map<String, Status> status = Map.of();
    private volatile Instant statusAt = Instant.EPOCH;

    public BikeService(HttpClient httpClient) {
        this.restTemplate = new RestTemplate(List.of(new JacksonJsonHttpMessageConverter()));
        this.restTemplate.setRequestFactory(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.DEFAULT_READ_TIMEOUT));
    }

    /**
     * Stations inside a box, at most {@link #MAX_STATIONS} (the closest to its center)
     */
    public List<BikeStation> getStations(double minLat, double minLon, double maxLat, double maxLon) {
        double lat = (minLat + maxLat) / 2;
        double lon = (minLon + maxLon) / 2;
        return stations().stream()
            .filter(station -> station.lat() >= minLat && station.lat() <= maxLat && station.lon() >= minLon && station.lon() <= maxLon)
            .sorted(Comparator.comparingDouble(station -> meters(lat, lon, station.lat(), station.lon())))
            .limit(MAX_STATIONS)
            .toList();
    }

    /**
     * Stations around a position, closest first, with their distance
     */
    public List<BikeStation> getNearby(double lat, double lon, int radius, int limit) {
        return stations().stream()
            .map(station -> withDistance(station, (int) Math.round(meters(lat, lon, station.lat(), station.lon()))))
            .filter(station -> station.distance() <= radius)
            .sorted(Comparator.comparingInt(BikeStation::distance))
            .limit(limit)
            .toList();
    }

    private List<BikeStation> stations() {
        refresh();
        Map<String, Status> statuses = status;
        List<BikeStation> stations = new ArrayList<>();
        for (Information info : information.values()) {
            Status s = statuses.get(info.id());
            if (s != null) {
                stations.add(new BikeStation(info.id(), info.code(), info.name(), info.lat(), info.lon(), info.capacity(),
                    s.mechanical(), s.electric(), s.docks(), s.renting(), s.returning(), null, s.reportedAt()));
            }
        }
        return stations;
    }

    private synchronized void refresh() {
        Instant now = Instant.now();
        if (informationAt.plus(INFORMATION_TTL).isBefore(now)) {
            try {
                Map<String, Information> next = new HashMap<>();
                for (JsonNode station : fetch("station_information").path("data").path("stations").values()) {
                    String id = station.path("station_id").asString();
                    next.put(id, new Information(id, station.path("stationCode").asString(null), station.path("name").asString(),
                        station.path("lat").asDouble(), station.path("lon").asDouble(), station.path("capacity").asInt()));
                }
                information = next;
                informationAt = now;
            } catch (RestClientException e) {
                log.warn("Vélib stations unavailable: {}", e.getMessage());
            }
        }
        if (statusAt.plus(STATUS_TTL).isBefore(now)) {
            try {
                Map<String, Status> next = new HashMap<>();
                for (JsonNode station : fetch("station_status").path("data").path("stations").values()) {
                    if (station.path("is_installed").asInt() != 1) {
                        continue;
                    }
                    int mechanical = 0;
                    int electric = 0;
                    for (JsonNode type : station.path("num_bikes_available_types").values()) {
                        mechanical += type.path("mechanical").asInt(0);
                        electric += type.path("ebike").asInt(0);
                    }
                    next.put(station.path("station_id").asString(), new Status(mechanical, electric, station.path("num_docks_available").asInt(),
                        station.path("is_renting").asInt() == 1, station.path("is_returning").asInt() == 1,
                        Instant.ofEpochSecond(station.path("last_reported").asLong())));
                }
                status = next;
                statusAt = now;
            } catch (RestClientException e) {
                log.warn("Vélib availability unavailable: {}", e.getMessage());
                // Not again before the next period
                statusAt = now.minus(STATUS_TTL).plusSeconds(15);
            }
        }
    }

    private JsonNode fetch(String feed) {
        return restTemplate.getForObject(Constants.VELIB_GBFS_BASE + "/" + feed + ".json", JsonNode.class);
    }

    private static BikeStation withDistance(BikeStation s, int distance) {
        return new BikeStation(s.id(), s.code(), s.name(), s.lat(), s.lon(), s.capacity(), s.mechanical(), s.electric(), s.docks(),
            s.renting(), s.returning(), distance, s.reportedAt());
    }

    /** Equirectangular approximation, enough at city scale */
    private static double meters(double lat1, double lon1, double lat2, double lon2) {
        double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = Math.toRadians(lat2 - lat1);
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }
}
