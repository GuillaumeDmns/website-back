package com.gdamiens.website.controller;

import com.gdamiens.website.idfm.navitia.*;
import com.gdamiens.website.service.IDFMNavitiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IDFMNavitiaController {

    private static final Logger log = LoggerFactory.getLogger(IDFMNavitiaController.class);

    private final IDFMNavitiaService idfmNavitiaService;

    public IDFMNavitiaController(IDFMNavitiaService idfmNavitiaService) {
        this.idfmNavitiaService = idfmNavitiaService;
    }

    @GetMapping({"/places", "/navitia/places"})
    @Operation(summary = "Get list of places for autocomplete", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Places> getPlaces(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer count) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getPlaces(query, type, count), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during IDFM get places: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getJourneys(startPoint, endPoint, datetime, datetimeRepresents, forbiddenUris, dataFreshness), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during IDFM get journeys: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping({"/stop/{stopPointId}/journeys", "/navitia/stop_points/{stopPointId}/vehicle_journeys"})
    @Operation(summary = "Get vehicle journeys from a stop point", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<VehicleJourneys> getStopPointJourneys(
            @PathVariable String stopPointId,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            @RequestParam(required = false, defaultValue = "3") Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getStopPointJourneys(stopPointId, since, until, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during IDFM get stop point journeys: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/lines")
    @Operation(summary = "Get lines list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Lines> getLines(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getLines(startPage, count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get lines: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/lines/{id}")
    @Operation(summary = "Get line by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Lines> getLineById(@PathVariable String id) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getLineById(id), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get line by ID: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/stop_areas")
    @Operation(summary = "Get stop areas list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopAreas> getStopAreas(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getStopAreas(startPage, count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get stop areas: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/stop_areas/{id}")
    @Operation(summary = "Get stop area by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopAreas> getStopAreaById(@PathVariable String id) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getStopAreaById(id), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get stop area by ID: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/stop_points")
    @Operation(summary = "Get stop points list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopPoints> getStopPoints(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getStopPoints(startPage, count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get stop points: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/stop_points/{id}")
    @Operation(summary = "Get stop point by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopPoints> getStopPointById(@PathVariable String id) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getStopPointById(id), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get stop point by ID: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/routes")
    @Operation(summary = "Get routes list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Routes> getRoutes(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getRoutes(startPage, count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get routes: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/routes/{id}")
    @Operation(summary = "Get route by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Routes> getRouteById(@PathVariable String id) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getRouteById(id), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get route by ID: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/networks")
    @Operation(summary = "Get networks list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Networks> getNetworks(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getNetworks(startPage, count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get networks: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/networks/{id}")
    @Operation(summary = "Get network by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Networks> getNetworkById(@PathVariable String id) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getNetworkById(id), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get network by ID: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/commercial_modes")
    @Operation(summary = "Get commercial modes list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<CommercialModes> getCommercialModes(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getCommercialModes(startPage, count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get commercial modes: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/commercial_modes/{id}")
    @Operation(summary = "Get commercial mode by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<CommercialModes> getCommercialModeById(@PathVariable String id) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getCommercialModeById(id), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get commercial mode by ID: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/physical_modes")
    @Operation(summary = "Get physical modes list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<PhysicalModes> getPhysicalModes(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getPhysicalModes(startPage, count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get physical modes: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/physical_modes/{id}")
    @Operation(summary = "Get physical mode by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<PhysicalModes> getPhysicalModeById(@PathVariable String id) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getPhysicalModeById(id), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get physical mode by ID: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/companies")
    @Operation(summary = "Get companies list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Companies> getCompanies(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getCompanies(startPage, count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get companies: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/companies/{id}")
    @Operation(summary = "Get company by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Companies> getCompanyById(@PathVariable String id) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getCompanyById(id), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get company by ID: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/disruptions")
    @Operation(summary = "Get disruptions list", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Disruptions> getDisruptions(
            @RequestParam(required = false) Integer startPage,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getDisruptions(startPage, count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get disruptions: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/disruptions/{id}")
    @Operation(summary = "Get disruption by ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Disruptions> getDisruptionById(@PathVariable String id) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getDisruptionById(id), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get disruption by ID: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/stop_points/{stopPointId}/departures")
    @Operation(summary = "Get departures for a stop point", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Departures> getDepartures(
            @PathVariable String stopPointId,
            @RequestParam(required = false) String fromDatetime,
            @RequestParam(required = false) String untilDatetime,
            @RequestParam(required = false) Integer count) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getDepartures(stopPointId, fromDatetime, untilDatetime, count), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get departures: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/stop_points/{stopPointId}/arrivals")
    @Operation(summary = "Get arrivals for a stop point", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Arrivals> getArrivals(
            @PathVariable String stopPointId,
            @RequestParam(required = false) String fromDatetime,
            @RequestParam(required = false) String untilDatetime,
            @RequestParam(required = false) Integer count) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getArrivals(stopPointId, fromDatetime, untilDatetime, count), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get arrivals: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/traffic_reports")
    @Operation(summary = "Get traffic reports", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<TrafficReports> getTrafficReports(
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getTrafficReports(count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get traffic reports: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/equipment_reports")
    @Operation(summary = "Get equipment reports", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<EquipmentReports> getEquipmentReports(
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getEquipmentReports(count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get equipment reports: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/routes/{routeId}/route_schedules")
    @Operation(summary = "Get route schedules", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<RouteSchedules> getRouteSchedules(
            @PathVariable String routeId,
            @RequestParam(required = false) String fromDatetime,
            @RequestParam(required = false) String untilDatetime,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getRouteSchedules(routeId, fromDatetime, untilDatetime, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get route schedules: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/stop_points/{stopPointId}/stop_schedules")
    @Operation(summary = "Get stop schedules", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopSchedules> getStopSchedules(
            @PathVariable String stopPointId,
            @RequestParam(required = false) String fromDatetime,
            @RequestParam(required = false) String untilDatetime,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getStopSchedules(stopPointId, fromDatetime, untilDatetime, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get stop schedules: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/places_nearby")
    @Operation(summary = "Get places nearby", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Places> getPlacesNearby(
            @RequestParam(required = false) Double distance,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer depth) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getPlacesNearby(distance, count, depth), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get places nearby: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/navitia/pt_objects")
    @Operation(summary = "Get public transport objects", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<PtObjects> getPtObjects(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer count) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getPtObjects(query, type, count), HttpStatus.OK);
        } catch (java.lang.Exception e) {
            log.error("Error during get pt_objects: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
