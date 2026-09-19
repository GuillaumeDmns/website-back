package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.navitia.*;
import com.gdamiens.website.utils.Constants;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class IDFMNavitiaService extends AbstractIDFMService {

    private static final Logger log = LoggerFactory.getLogger(IDFMNavitiaService.class);

    private final RestTemplate restTemplate;

    public IDFMNavitiaService(ApplicationProperties applicationProperties) {
        super(applicationProperties);
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build());
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public Places getPlaces(String query) {
        return getPlaces(query, null, null);
    }

    public Places getPlaces(String query, String type, Integer count) {
        log.info("Getting places for query {}", query);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_PLACES);
        if (query != null) uriComponentsBuilder.queryParam("q", query);
        if (type != null) uriComponentsBuilder.queryParam("type[]", type);
        if (count != null) uriComponentsBuilder.queryParam("count", count);

        ResponseEntity<Places> response = this.restTemplate.exchange(uriComponentsBuilder.build().toUri(), HttpMethod.GET, request, Places.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException("IDFM Navitia places response != 200", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response.getBody();
    }

    public Journeys getJourneys(String startPoint, String endPoint) {
        return getJourneys(startPoint, endPoint, null, null, null, "realtime");
    }

    public Journeys getJourneys(String startPoint, String endPoint, String datetime, String datetimeRepresents, List<String> forbiddenUris, String dataFreshness) {
        log.info("Getting journeys for start point {} and end point {}", startPoint, endPoint);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_JOURNEYS)
            .queryParam("data_freshness", dataFreshness != null ? dataFreshness : "realtime")
            .queryParam("from", startPoint)
            .queryParam("to", endPoint);

        if (datetime != null) uriComponentsBuilder.queryParam("datetime", datetime);
        if (datetimeRepresents != null) uriComponentsBuilder.queryParam("datetime_represents", datetimeRepresents);
        if (forbiddenUris != null && !forbiddenUris.isEmpty()) {
            for (String uri : forbiddenUris) {
                uriComponentsBuilder.queryParam("forbidden_uris[]", uri);
            }
        }

        ResponseEntity<Journeys> response = this.restTemplate.exchange(uriComponentsBuilder.build().toUri(), HttpMethod.GET, request, Journeys.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException("IDFM Navitia journeys response != 200", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response.getBody();
    }

    public VehicleJourneys getStopPointJourneys(String stopPointId, String since, String until) {
        return getStopPointJourneys(stopPointId, since, until, 3);
    }

    public VehicleJourneys getStopPointJourneys(String stopPointId, String since, String until, Integer depth) {
        log.info("Getting next departures for stop point {}", stopPointId);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE)
            .pathSegment("stop_points", stopPointId, "vehicle_journeys")
            .queryParam("data_freshness", "realtime")
            .queryParam("depth", depth != null ? depth : 3);

        if (since != null) uriComponentsBuilder.queryParam("since", since);
        if (until != null) uriComponentsBuilder.queryParam("until", until);

        ResponseEntity<VehicleJourneys> response = this.restTemplate.exchange(uriComponentsBuilder.build().toUri(), HttpMethod.GET, request, VehicleJourneys.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException("IDFM Navitia stop point journeys response != 200", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response.getBody();
    }

    public Lines getLines(Integer startPage, Integer count, Integer depth) {
        log.info("Getting lines");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("lines");
        if (startPage != null) builder.queryParam("start_page", startPage);
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<Lines> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Lines.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia lines error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Lines getLineById(String id) {
        log.info("Getting line {}", id);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("lines", id);
        ResponseEntity<Lines> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Lines.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia line error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public StopAreas getStopAreas(Integer startPage, Integer count, Integer depth) {
        log.info("Getting stop areas");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("stop_areas");
        if (startPage != null) builder.queryParam("start_page", startPage);
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<StopAreas> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, StopAreas.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia stop_areas error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public StopAreas getStopAreaById(String id) {
        log.info("Getting stop area {}", id);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("stop_areas", id);
        ResponseEntity<StopAreas> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, StopAreas.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia stop_area error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public StopPoints getStopPoints(Integer startPage, Integer count, Integer depth) {
        log.info("Getting stop points");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("stop_points");
        if (startPage != null) builder.queryParam("start_page", startPage);
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<StopPoints> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, StopPoints.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia stop_points error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public StopPoints getStopPointById(String id) {
        log.info("Getting stop point {}", id);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("stop_points", id);
        ResponseEntity<StopPoints> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, StopPoints.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia stop_point error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Routes getRoutes(Integer startPage, Integer count, Integer depth) {
        log.info("Getting routes");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("routes");
        if (startPage != null) builder.queryParam("start_page", startPage);
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<Routes> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Routes.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia routes error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Routes getRouteById(String id) {
        log.info("Getting route {}", id);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("routes", id);
        ResponseEntity<Routes> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Routes.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia route error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Networks getNetworks(Integer startPage, Integer count, Integer depth) {
        log.info("Getting networks");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("networks");
        if (startPage != null) builder.queryParam("start_page", startPage);
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<Networks> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Networks.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia networks error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Networks getNetworkById(String id) {
        log.info("Getting network {}", id);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("networks", id);
        ResponseEntity<Networks> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Networks.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia network error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public CommercialModes getCommercialModes(Integer startPage, Integer count, Integer depth) {
        log.info("Getting commercial modes");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("commercial_modes");
        if (startPage != null) builder.queryParam("start_page", startPage);
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<CommercialModes> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, CommercialModes.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia commercial_modes error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public CommercialModes getCommercialModeById(String id) {
        log.info("Getting commercial mode {}", id);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("commercial_modes", id);
        ResponseEntity<CommercialModes> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, CommercialModes.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia commercial_mode error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public PhysicalModes getPhysicalModes(Integer startPage, Integer count, Integer depth) {
        log.info("Getting physical modes");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("physical_modes");
        if (startPage != null) builder.queryParam("start_page", startPage);
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<PhysicalModes> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, PhysicalModes.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia physical_modes error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public PhysicalModes getPhysicalModeById(String id) {
        log.info("Getting physical mode {}", id);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("physical_modes", id);
        ResponseEntity<PhysicalModes> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, PhysicalModes.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia physical_mode error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Companies getCompanies(Integer startPage, Integer count, Integer depth) {
        log.info("Getting companies");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("companies");
        if (startPage != null) builder.queryParam("start_page", startPage);
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<Companies> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Companies.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia companies error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Companies getCompanyById(String id) {
        log.info("Getting company {}", id);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("companies", id);
        ResponseEntity<Companies> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Companies.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia company error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Disruptions getDisruptions(Integer startPage, Integer count, Integer depth) {
        log.info("Getting disruptions");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("disruptions");
        if (startPage != null) builder.queryParam("start_page", startPage);
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<Disruptions> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Disruptions.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia disruptions error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Disruptions getDisruptionById(String id) {
        log.info("Getting disruption {}", id);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("disruptions", id);
        ResponseEntity<Disruptions> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Disruptions.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia disruption error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Departures getDepartures(String stopPointId, String fromDatetime, String untilDatetime, Integer count) {
        log.info("Getting departures for stop point {}", stopPointId);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE)
            .pathSegment("stop_points", stopPointId, "departures");
        if (fromDatetime != null) builder.queryParam("from_datetime", fromDatetime);
        if (untilDatetime != null) builder.queryParam("until_datetime", untilDatetime);
        if (count != null) builder.queryParam("count", count);

        ResponseEntity<Departures> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Departures.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia departures error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Arrivals getArrivals(String stopPointId, String fromDatetime, String untilDatetime, Integer count) {
        log.info("Getting arrivals for stop point {}", stopPointId);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE)
            .pathSegment("stop_points", stopPointId, "arrivals");
        if (fromDatetime != null) builder.queryParam("from_datetime", fromDatetime);
        if (untilDatetime != null) builder.queryParam("until_datetime", untilDatetime);
        if (count != null) builder.queryParam("count", count);

        ResponseEntity<Arrivals> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Arrivals.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia arrivals error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public TrafficReports getTrafficReports(Integer count, Integer depth) {
        log.info("Getting traffic reports");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("traffic_reports");
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<TrafficReports> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, TrafficReports.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia traffic_reports error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public EquipmentReports getEquipmentReports(Integer count, Integer depth) {
        log.info("Getting equipment reports");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("equipment_reports");
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<EquipmentReports> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, EquipmentReports.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia equipment_reports error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public RouteSchedules getRouteSchedules(String routeId, String fromDatetime, String untilDatetime, Integer depth) {
        log.info("Getting route schedules for route {}", routeId);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE)
            .pathSegment("routes", routeId, "route_schedules");
        if (fromDatetime != null) builder.queryParam("from_datetime", fromDatetime);
        if (untilDatetime != null) builder.queryParam("until_datetime", untilDatetime);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<RouteSchedules> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, RouteSchedules.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia route_schedules error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public StopSchedules getStopSchedules(String stopPointId, String fromDatetime, String untilDatetime, Integer depth) {
        log.info("Getting stop schedules for stop point {}", stopPointId);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE)
            .pathSegment("stop_points", stopPointId, "stop_schedules");
        if (fromDatetime != null) builder.queryParam("from_datetime", fromDatetime);
        if (untilDatetime != null) builder.queryParam("until_datetime", untilDatetime);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<StopSchedules> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, StopSchedules.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia stop_schedules error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public Places getPlacesNearby(Double distance, Integer count, Integer depth) {
        log.info("Getting places nearby");
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("places_nearby");
        if (distance != null) builder.queryParam("distance", distance);
        if (count != null) builder.queryParam("count", count);
        if (depth != null) builder.queryParam("depth", depth);

        ResponseEntity<Places> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, Places.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia places_nearby error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }

    public PtObjects getPtObjects(String query, String type, Integer count) {
        log.info("Getting pt_objects for query {}", query);
        HttpEntity<String> request = this.prepareHttpRequest();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment("pt_objects");
        if (query != null) builder.queryParam("q", query);
        if (type != null) builder.queryParam("type[]", type);
        if (count != null) builder.queryParam("count", count);

        ResponseEntity<PtObjects> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request, PtObjects.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new CustomException("IDFM Navitia pt_objects error", HttpStatus.INTERNAL_SERVER_ERROR);
        return response.getBody();
    }
}
