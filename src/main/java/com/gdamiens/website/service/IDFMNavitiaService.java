package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.NavitiaException;
import com.gdamiens.website.idfm.navitia.*;
import com.gdamiens.website.utils.Constants;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

/**
 * Client of the IDFM PRIM Navitia API (swagger.json at the project root)
 */
@Service
public class IDFMNavitiaService extends AbstractIDFMService {

    private static final Logger log = LoggerFactory.getLogger(IDFMNavitiaService.class);

    private final RestTemplate restTemplate;

    public IDFMNavitiaService(ApplicationProperties applicationProperties) {
        super(applicationProperties);
        // Only JSON responses; an enum value added by Navitia must not make the whole response fail
        this.restTemplate = new RestTemplate(List.of(new JacksonJsonHttpMessageConverter(
            JsonMapper.builder().enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL).build()
        )));
        this.restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build()));
    }

    public Places getPlaces(String query, List<String> types, Integer count) {
        log.info("Getting places for query {}", query);
        UriComponentsBuilder builder = navitia("places")
            .queryParam("q", query)
            .queryParamIfPresent("count", Optional.ofNullable(count));
        addAll(builder, "type[]", types);
        return get(builder, Places.class);
    }

    public Journeys getJourneys(String startPoint, String endPoint, String datetime, String datetimeRepresents, List<String> forbiddenUris, String dataFreshness) {
        log.info("Getting journeys for start point {} and end point {}", startPoint, endPoint);
        UriComponentsBuilder builder = navitia("journeys")
            .queryParam("data_freshness", dataFreshness != null ? dataFreshness : "realtime")
            .queryParam("from", startPoint)
            .queryParam("to", endPoint)
            .queryParamIfPresent("datetime", Optional.ofNullable(datetime))
            .queryParamIfPresent("datetime_represents", Optional.ofNullable(datetimeRepresents));
        addAll(builder, "forbidden_uris[]", forbiddenUris);
        return get(builder, Journeys.class);
    }

    /**
     * Journey planning for the v2 API, with all the options and the raw response (snake_case Navitia JSON): the
     * mapping only reads the fields it needs.
     *
     * @param from          {@code lon;lat} or a Navitia id ({@code stop_area:IDFM:71264})
     * @param datetime      local Paris time, {@code yyyyMMdd'T'HHmmss}
     * @param walkingSpeed  meters per second
     */
    public JsonNode planJourneys(String from, String to, String datetime, boolean arriveBy, List<String> forbiddenUris,
                                 boolean wheelchair, Double walkingSpeed, Integer maxTransfers) {
        log.info("Planning journeys from {} to {}", from, to);
        UriComponentsBuilder builder = navitia("journeys")
            .queryParam("from", from)
            .queryParam("to", to)
            .queryParam("data_freshness", "realtime")
            .queryParam("min_nb_journeys", 4)
            .queryParam("datetime_represents", arriveBy ? "arrival" : "departure")
            .queryParamIfPresent("datetime", Optional.ofNullable(datetime))
            .queryParamIfPresent("walking_speed", Optional.ofNullable(walkingSpeed))
            .queryParamIfPresent("max_nb_transfers", Optional.ofNullable(maxTransfers));
        if (wheelchair) {
            builder.queryParam("wheelchair", true);
        }
        addAll(builder, "forbidden_uris[]", forbiddenUris);
        return get(builder, JsonNode.class);
    }

    /**
     * Walking path only (no public transport) between two places, raw Navitia JSON. PRIM only computes walking paths:
     * other street modes ({@code bike}, {@code bss}) come back as walking.
     *
     * @param walkingSpeed meters per second
     */
    public JsonNode planWalkingPath(String from, String to, String datetime, Double walkingSpeed) {
        log.info("Planning a walking path from {} to {}", from, to);
        UriComponentsBuilder builder = navitia("journeys")
            .queryParam("from", from)
            .queryParam("to", to)
            .queryParam("direct_path", "only")
            .queryParam("direct_path_mode[]", "walking")
            .queryParamIfPresent("datetime", Optional.ofNullable(datetime))
            .queryParamIfPresent("walking_speed", Optional.ofNullable(walkingSpeed));
        return get(builder, JsonNode.class);
    }

    /**
     * Every current and upcoming disruption, grouped by network, line and stop area (raw snake_case Navitia JSON:
     * the mapping only reads the fields it needs).
     */
    public JsonNode getAllTrafficReports() {
        log.info("Getting all traffic reports");
        UriComponentsBuilder builder = navitia("traffic_reports")
            .queryParam("count", 1000)
            .queryParam("depth", 0)
            .queryParam("disable_geojson", true);
        return get(builder, JsonNode.class);
    }

    public VehicleJourneys getStopPointJourneys(String stopPointId, String since, String until, Integer depth) {
        log.info("Getting vehicle journeys for stop point {}", stopPointId);
        UriComponentsBuilder builder = navitia("stop_points", stopPointId, "vehicle_journeys")
            .queryParam("depth", depth != null ? depth : 3)
            .queryParamIfPresent("since", Optional.ofNullable(since))
            .queryParamIfPresent("until", Optional.ofNullable(until));
        return get(builder, VehicleJourneys.class);
    }

    public Lines getLines(Integer startPage, Integer count, Integer depth) {
        log.info("Getting lines");
        return get(paginated(navitia("lines"), startPage, count, depth), Lines.class);
    }

    public Lines getLineById(String id) {
        log.info("Getting line {}", id);
        return get(navitia("lines", id), Lines.class);
    }

    public StopAreas getStopAreas(Integer startPage, Integer count, Integer depth) {
        log.info("Getting stop areas");
        return get(paginated(navitia("stop_areas"), startPage, count, depth), StopAreas.class);
    }

    public StopAreas getStopAreaById(String id) {
        log.info("Getting stop area {}", id);
        return get(navitia("stop_areas", id), StopAreas.class);
    }

    public StopPoints getStopPoints(Integer startPage, Integer count, Integer depth) {
        log.info("Getting stop points");
        return get(paginated(navitia("stop_points"), startPage, count, depth), StopPoints.class);
    }

    public StopPoints getStopPointById(String id) {
        log.info("Getting stop point {}", id);
        return get(navitia("stop_points", id), StopPoints.class);
    }

    public Routes getRoutes(Integer startPage, Integer count, Integer depth) {
        log.info("Getting routes");
        return get(paginated(navitia("routes"), startPage, count, depth), Routes.class);
    }

    public Routes getRouteById(String id) {
        log.info("Getting route {}", id);
        return get(navitia("routes", id), Routes.class);
    }

    public Networks getNetworks(Integer startPage, Integer count, Integer depth) {
        log.info("Getting networks");
        return get(paginated(navitia("networks"), startPage, count, depth), Networks.class);
    }

    public Networks getNetworkById(String id) {
        log.info("Getting network {}", id);
        return get(navitia("networks", id), Networks.class);
    }

    public CommercialModes getCommercialModes(Integer startPage, Integer count, Integer depth) {
        log.info("Getting commercial modes");
        return get(paginated(navitia("commercial_modes"), startPage, count, depth), CommercialModes.class);
    }

    public CommercialModes getCommercialModeById(String id) {
        log.info("Getting commercial mode {}", id);
        return get(navitia("commercial_modes", id), CommercialModes.class);
    }

    public PhysicalModes getPhysicalModes(Integer startPage, Integer count, Integer depth) {
        log.info("Getting physical modes");
        return get(paginated(navitia("physical_modes"), startPage, count, depth), PhysicalModes.class);
    }

    public PhysicalModes getPhysicalModeById(String id) {
        log.info("Getting physical mode {}", id);
        return get(navitia("physical_modes", id), PhysicalModes.class);
    }

    public Companies getCompanies(Integer startPage, Integer count, Integer depth) {
        log.info("Getting companies");
        return get(paginated(navitia("companies"), startPage, count, depth), Companies.class);
    }

    public Companies getCompanyById(String id) {
        log.info("Getting company {}", id);
        return get(navitia("companies", id), Companies.class);
    }

    public Disruptions getDisruptions(Integer startPage, Integer count, Integer depth) {
        log.info("Getting disruptions");
        return get(paginated(navitia("disruptions"), startPage, count, depth), Disruptions.class);
    }

    public Disruptions getDisruptionById(String id) {
        log.info("Getting disruption {}", id);
        return get(navitia("disruptions", id), Disruptions.class);
    }

    public Departures getDepartures(String stopPointId, String fromDatetime, String untilDatetime, Integer count, String dataFreshness) {
        log.info("Getting departures for stop point {}", stopPointId);
        UriComponentsBuilder builder = schedule(navitia("stop_points", stopPointId, "departures"), fromDatetime, untilDatetime, dataFreshness)
            .queryParamIfPresent("count", Optional.ofNullable(count));
        return get(builder, Departures.class);
    }

    public Arrivals getArrivals(String stopPointId, String fromDatetime, String untilDatetime, Integer count, String dataFreshness) {
        log.info("Getting arrivals for stop point {}", stopPointId);
        UriComponentsBuilder builder = schedule(navitia("stop_points", stopPointId, "arrivals"), fromDatetime, untilDatetime, dataFreshness)
            .queryParamIfPresent("count", Optional.ofNullable(count));
        return get(builder, Arrivals.class);
    }

    public TrafficReports getTrafficReports(Integer count, Integer depth) {
        log.info("Getting traffic reports");
        UriComponentsBuilder builder = navitia("traffic_reports")
            .queryParamIfPresent("count", Optional.ofNullable(count))
            .queryParamIfPresent("depth", Optional.ofNullable(depth));
        return get(builder, TrafficReports.class);
    }

    public EquipmentReports getEquipmentReports(Integer count, Integer depth) {
        log.info("Getting equipment reports");
        UriComponentsBuilder builder = navitia("equipment_reports")
            .queryParamIfPresent("count", Optional.ofNullable(count))
            .queryParamIfPresent("depth", Optional.ofNullable(depth));
        return get(builder, EquipmentReports.class);
    }

    public RouteSchedules getRouteSchedules(String routeId, String fromDatetime, String untilDatetime, Integer depth, String dataFreshness) {
        log.info("Getting route schedules for route {}", routeId);
        UriComponentsBuilder builder = schedule(navitia("routes", routeId, "route_schedules"), fromDatetime, untilDatetime, dataFreshness)
            .queryParamIfPresent("depth", Optional.ofNullable(depth));
        return get(builder, RouteSchedules.class);
    }

    public StopSchedules getStopSchedules(String stopPointId, String fromDatetime, String untilDatetime, Integer depth, String dataFreshness) {
        log.info("Getting stop schedules for stop point {}", stopPointId);
        UriComponentsBuilder builder = schedule(navitia("stop_points", stopPointId, "stop_schedules"), fromDatetime, untilDatetime, dataFreshness)
            .queryParamIfPresent("depth", Optional.ofNullable(depth));
        return get(builder, StopSchedules.class);
    }

    public PlacesNearby getPlacesNearby(Double longitude, Double latitude, List<String> types, Double distance, Integer count, Integer depth) {
        log.info("Getting places nearby {};{}", longitude, latitude);
        UriComponentsBuilder builder = navitia("coords", longitude + ";" + latitude, "places_nearby")
            .queryParamIfPresent("distance", Optional.ofNullable(distance))
            .queryParamIfPresent("count", Optional.ofNullable(count))
            .queryParamIfPresent("depth", Optional.ofNullable(depth));
        addAll(builder, "type[]", types);
        return get(builder, PlacesNearby.class);
    }

    public PtObjects getPtObjects(String query, List<String> types, Integer count) {
        log.info("Getting pt_objects for query {}", query);
        UriComponentsBuilder builder = navitia("pt_objects")
            .queryParam("q", query)
            .queryParamIfPresent("count", Optional.ofNullable(count));
        addAll(builder, "type[]", types);
        return get(builder, PtObjects.class);
    }

    private static UriComponentsBuilder navitia(String... pathSegments) {
        return UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment(pathSegments);
    }

    private static UriComponentsBuilder paginated(UriComponentsBuilder builder, Integer startPage, Integer count, Integer depth) {
        return builder
            .queryParamIfPresent("start_page", Optional.ofNullable(startPage))
            .queryParamIfPresent("count", Optional.ofNullable(count))
            .queryParamIfPresent("depth", Optional.ofNullable(depth));
    }

    private static UriComponentsBuilder schedule(UriComponentsBuilder builder, String fromDatetime, String untilDatetime, String dataFreshness) {
        return builder
            .queryParamIfPresent("from_datetime", Optional.ofNullable(fromDatetime))
            .queryParamIfPresent("until_datetime", Optional.ofNullable(untilDatetime))
            .queryParamIfPresent("data_freshness", Optional.ofNullable(dataFreshness));
    }

    private static void addAll(UriComponentsBuilder builder, String name, List<String> values) {
        if (values != null) {
            values.forEach(value -> builder.queryParam(name, value));
        }
    }

    // Values are percent-encoded (accents, &, = in q...); Navitia errors are rethrown with their status and body
    private <T> T get(UriComponentsBuilder builder, Class<T> responseType) {
        try {
            return this.restTemplate.exchange(builder.encode().build().toUri(), HttpMethod.GET, this.prepareHttpRequest(), responseType).getBody();
        } catch (HttpStatusCodeException e) {
            throw new NavitiaException(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }
}
