package com.gdamiens.website.controller;

import com.gdamiens.website.exceptions.NavitiaException;
import com.gdamiens.website.idfm.navitia.*;
import com.gdamiens.website.service.IDFMNavitiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IDFMNavitiaController {

    private static final Logger log = LoggerFactory.getLogger(IDFMNavitiaController.class);

    private final IDFMNavitiaService idfmNavitiaService;

    public IDFMNavitiaController(IDFMNavitiaService idfmNavitiaService) {
        this.idfmNavitiaService = idfmNavitiaService;
    }

    // Client errors (unknown id, bad parameter...) keep the Navitia status and error body. Server errors and
    // 401/403 (our IDFM API key, not the caller's JWT) become a bad gateway
    @ExceptionHandler(NavitiaException.class)
    public ResponseEntity<String> handleNavitiaException(NavitiaException e) {
        log.error("Error during IDFM Navitia request: {}", e.getMessage());
        int code = e.getStatusCode().value();
        HttpStatus status = e.getStatusCode().is4xxClientError() && code != 401 && code != 403 ? HttpStatus.valueOf(code) : HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(e.getResponseBody());
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Void> handleRestClientException(RestClientException e) {
        log.error("Error during IDFM Navitia request: {}", e.getMessage(), e);
        return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
    }

    @GetMapping({"/places", "/navitia/places"})
    @Operation(summary = "Get list of places for autocomplete", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Places> getPlaces(
            @RequestParam String query,
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) Integer count) {
        return ResponseEntity.ok(this.idfmNavitiaService.getPlaces(query, type, count));
    }

    @GetMapping({"/journeys", "/navitia/journeys"})
    @Operation(summary = "Get calculated journeys", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Journeys> getJourneys(
            @RequestParam String startPoint,
            @RequestParam String endPoint,
            @RequestParam(required = false) String datetime,
            @RequestParam(required = false) String datetimeRepresents,
            @RequestParam(required = false) List<String> forbiddenUris,
            @RequestParam(required = false, defaultValue = "realtime") String dataFreshness) {
        return ResponseEntity.ok(this.idfmNavitiaService.getJourneys(startPoint, endPoint, datetime, datetimeRepresents, forbiddenUris, dataFreshness));
    }

    @GetMapping({"/stop/{stopPointId}/journeys", "/navitia/stop_points/{stopPointId}/vehicle_journeys"})
    @Operation(summary = "Get vehicle journeys from a stop point", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<VehicleJourneys> getStopPointJourneys(
            @PathVariable String stopPointId,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            @RequestParam(required = false, defaultValue = "3") Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getStopPointJourneys(stopPointId, since, until, depth));
    }

    @GetMapping("/navitia/lines")
    @Operation(summary = "Get lines list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Lines> getLines(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getLines(startPage, count, depth));
    }

    @GetMapping("/navitia/lines/{id}")
    @Operation(summary = "Get line by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Lines> getLineById(@PathVariable String id) {
        return ResponseEntity.ok(this.idfmNavitiaService.getLineById(id));
    }

    @GetMapping("/navitia/stop_areas")
    @Operation(summary = "Get stop areas list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopAreas> getStopAreas(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getStopAreas(startPage, count, depth));
    }

    @GetMapping("/navitia/stop_areas/{id}")
    @Operation(summary = "Get stop area by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopAreas> getStopAreaById(@PathVariable String id) {
        return ResponseEntity.ok(this.idfmNavitiaService.getStopAreaById(id));
    }

    @GetMapping("/navitia/stop_points")
    @Operation(summary = "Get stop points list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopPoints> getStopPoints(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getStopPoints(startPage, count, depth));
    }

    @GetMapping("/navitia/stop_points/{id}")
    @Operation(summary = "Get stop point by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopPoints> getStopPointById(@PathVariable String id) {
        return ResponseEntity.ok(this.idfmNavitiaService.getStopPointById(id));
    }

    @GetMapping("/navitia/routes")
    @Operation(summary = "Get routes list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Routes> getRoutes(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getRoutes(startPage, count, depth));
    }

    @GetMapping("/navitia/routes/{id}")
    @Operation(summary = "Get route by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Routes> getRouteById(@PathVariable String id) {
        return ResponseEntity.ok(this.idfmNavitiaService.getRouteById(id));
    }

    @GetMapping("/navitia/networks")
    @Operation(summary = "Get networks list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Networks> getNetworks(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getNetworks(startPage, count, depth));
    }

    @GetMapping("/navitia/networks/{id}")
    @Operation(summary = "Get network by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Networks> getNetworkById(@PathVariable String id) {
        return ResponseEntity.ok(this.idfmNavitiaService.getNetworkById(id));
    }

    @GetMapping("/navitia/commercial_modes")
    @Operation(summary = "Get commercial modes list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<CommercialModes> getCommercialModes(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getCommercialModes(startPage, count, depth));
    }

    @GetMapping("/navitia/commercial_modes/{id}")
    @Operation(summary = "Get commercial mode by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<CommercialModes> getCommercialModeById(@PathVariable String id) {
        return ResponseEntity.ok(this.idfmNavitiaService.getCommercialModeById(id));
    }

    @GetMapping("/navitia/physical_modes")
    @Operation(summary = "Get physical modes list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<PhysicalModes> getPhysicalModes(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getPhysicalModes(startPage, count, depth));
    }

    @GetMapping("/navitia/physical_modes/{id}")
    @Operation(summary = "Get physical mode by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<PhysicalModes> getPhysicalModeById(@PathVariable String id) {
        return ResponseEntity.ok(this.idfmNavitiaService.getPhysicalModeById(id));
    }

    @GetMapping("/navitia/companies")
    @Operation(summary = "Get companies list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Companies> getCompanies(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getCompanies(startPage, count, depth));
    }

    @GetMapping("/navitia/companies/{id}")
    @Operation(summary = "Get company by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Companies> getCompanyById(@PathVariable String id) {
        return ResponseEntity.ok(this.idfmNavitiaService.getCompanyById(id));
    }

    @GetMapping("/navitia/disruptions")
    @Operation(summary = "Get disruptions list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Disruptions> getDisruptions(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getDisruptions(startPage, count, depth));
    }

    @GetMapping("/navitia/disruptions/{id}")
    @Operation(summary = "Get disruption by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Disruptions> getDisruptionById(@PathVariable String id) {
        return ResponseEntity.ok(this.idfmNavitiaService.getDisruptionById(id));
    }

    @GetMapping("/navitia/stop_points/{stopPointId}/departures")
    @Operation(summary = "Get departures for a stop point", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Departures> getDepartures(
            @PathVariable String stopPointId,
            @RequestParam(required = false) String fromDatetime,
            @RequestParam(required = false) String untilDatetime,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) String dataFreshness) {
        return ResponseEntity.ok(this.idfmNavitiaService.getDepartures(stopPointId, fromDatetime, untilDatetime, count, dataFreshness));
    }

    @GetMapping("/navitia/stop_points/{stopPointId}/arrivals")
    @Operation(summary = "Get arrivals for a stop point", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Arrivals> getArrivals(
            @PathVariable String stopPointId,
            @RequestParam(required = false) String fromDatetime,
            @RequestParam(required = false) String untilDatetime,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) String dataFreshness) {
        return ResponseEntity.ok(this.idfmNavitiaService.getArrivals(stopPointId, fromDatetime, untilDatetime, count, dataFreshness));
    }

    @GetMapping("/navitia/traffic_reports")
    @Operation(summary = "Get traffic reports", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<TrafficReports> getTrafficReports(
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getTrafficReports(count, depth));
    }

    @GetMapping("/navitia/equipment_reports")
    @Operation(summary = "Get equipment reports", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<EquipmentReports> getEquipmentReports(
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getEquipmentReports(count, depth));
    }

    @GetMapping("/navitia/routes/{routeId}/route_schedules")
    @Operation(summary = "Get route schedules", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<RouteSchedules> getRouteSchedules(
            @PathVariable String routeId,
            @RequestParam(required = false) String fromDatetime,
            @RequestParam(required = false) String untilDatetime,
            @RequestParam(required = false) Integer depth,
            @RequestParam(required = false) String dataFreshness) {
        return ResponseEntity.ok(this.idfmNavitiaService.getRouteSchedules(routeId, fromDatetime, untilDatetime, depth, dataFreshness));
    }

    @GetMapping("/navitia/stop_points/{stopPointId}/stop_schedules")
    @Operation(summary = "Get stop schedules", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopSchedules> getStopSchedules(
            @PathVariable String stopPointId,
            @RequestParam(required = false) String fromDatetime,
            @RequestParam(required = false) String untilDatetime,
            @RequestParam(required = false) Integer depth,
            @RequestParam(required = false) String dataFreshness) {
        return ResponseEntity.ok(this.idfmNavitiaService.getStopSchedules(stopPointId, fromDatetime, untilDatetime, depth, dataFreshness));
    }

    @GetMapping("/navitia/places_nearby")
    @Operation(summary = "Get places nearby a coordinate", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<PlacesNearby> getPlacesNearby(
            @RequestParam Double lon,
            @RequestParam Double lat,
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) Double distance,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(this.idfmNavitiaService.getPlacesNearby(lon, lat, type, distance, count, depth));
    }

    @GetMapping("/navitia/pt_objects")
    @Operation(summary = "Get public transport objects", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<PtObjects> getPtObjects(
            @RequestParam String query,
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) Integer count) {
        return ResponseEntity.ok(this.idfmNavitiaService.getPtObjects(query, type, count));
    }
}
