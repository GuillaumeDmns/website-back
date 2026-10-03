package com.gdamiens.website.controller.v2;

import com.gdamiens.website.controller.object.v2.JourneyPlan;
import com.gdamiens.website.controller.object.v2.LineDetail;
import com.gdamiens.website.controller.object.v2.SearchResult;
import com.gdamiens.website.controller.object.v2.StopAreaDetail;
import com.gdamiens.website.controller.object.v2.StopAreaSummary;
import com.gdamiens.website.controller.object.v2.StopDepartures;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.service.DepartureService;
import com.gdamiens.website.service.JourneyService;
import com.gdamiens.website.service.JourneyService.JourneyQuery;
import com.gdamiens.website.service.JourneyService.WalkingSpeed;
import com.gdamiens.website.service.NetworkService;
import com.gdamiens.website.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * Mobility endpoints for the apps: compact DTOs, stop areas ({@code IDFM:71264}) and IDFM line ids ({@code C01371})
 * everywhere.
 */
@RestController
@RequestMapping("/api/v2")
public class MobilityController {

    private static final int MAX_RADIUS = 2000;

    private final NetworkService networkService;

    private final DepartureService departureService;

    private final SearchService searchService;

    private final JourneyService journeyService;

    public MobilityController(NetworkService networkService, DepartureService departureService, SearchService searchService,
                              JourneyService journeyService) {
        this.networkService = networkService;
        this.departureService = departureService;
        this.searchService = searchService;
        this.journeyService = journeyService;
    }

    @GetMapping("/journeys")
    @Operation(summary = "Journey options between two places, real time when available", security = @SecurityRequirement(name = "Auth. Token"))
    public JourneyPlan getJourneys(
        @Parameter(description = "lat,lon or stop area id", example = "48.8443,2.3730") @RequestParam String from,
        @Parameter(description = "lat,lon or stop area id", example = "IDFM:71264") @RequestParam String to,
        @Parameter(description = "ISO-8601 instant (e.g. 2026-10-03T08:30:00Z), now when absent") @RequestParam(required = false) Instant datetime,
        @Parameter(description = "datetime is the arrival time") @RequestParam(defaultValue = "false") boolean arriveBy,
        @Parameter(description = "Allowed public transport modes, all when absent") @RequestParam(required = false) List<TransportMode> modes,
        @Parameter(description = "Step-free journeys only") @RequestParam(defaultValue = "false") boolean wheelchair,
        @RequestParam(defaultValue = "NORMAL") WalkingSpeed walkingSpeed,
        @Parameter(description = "At most this many transfers") @RequestParam(required = false) Integer maxTransfers) {
        return journeyService.plan(new JourneyQuery(from, to, datetime, arriveBy, modes, wheelchair, walkingSpeed, maxTransfers));
    }

    @GetMapping("/search")
    @Operation(summary = "Search lines, stop areas, addresses and points of interest", security = @SecurityRequirement(name = "Auth. Token"))
    public SearchResult search(@Parameter(example = "chatelet") @RequestParam String q,
                               @Parameter(description = "Places, at most 20") @RequestParam(defaultValue = "10") int limit) {
        return searchService.search(q, Math.clamp(limit, 1, 20));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Stop areas around a position with their lines, closest first", security = @SecurityRequirement(name = "Auth. Token"))
    public List<StopAreaSummary> getNearby(@RequestParam double lat, @RequestParam double lon,
                                           @Parameter(description = "Meters, at most 2000") @RequestParam(defaultValue = "500") int radius,
                                           @Parameter(description = "At most 50") @RequestParam(defaultValue = "20") int limit) {
        checkPosition(lat, lon);
        return networkService.getNearbyStopAreas(lat, lon, Math.clamp(radius, 1, MAX_RADIUS), Math.clamp(limit, 1, 50));
    }

    @GetMapping("/nearby/departures")
    @Operation(summary = "Next departures of the stop areas around a position, closest first", security = @SecurityRequirement(name = "Auth. Token"))
    public List<StopDepartures> getNearbyDepartures(@RequestParam double lat, @RequestParam double lon,
                                                    @Parameter(description = "Meters, at most 2000") @RequestParam(defaultValue = "500") int radius,
                                                    @Parameter(description = "Stop areas, at most 10") @RequestParam(defaultValue = "5") int maxStops,
                                                    @Parameter(description = "Departures per line and destination, at most 5") @RequestParam(defaultValue = "3") int limit) {
        checkPosition(lat, lon);
        return departureService.getNearbyDepartures(lat, lon, Math.clamp(radius, 1, MAX_RADIUS), Math.clamp(maxStops, 1, 10), limit);
    }

    @GetMapping("/stops/{stopAreaId}")
    @Operation(summary = "Stop area with its lines, quays and walking connections", security = @SecurityRequirement(name = "Auth. Token"))
    public StopAreaDetail getStopArea(@Parameter(example = "IDFM:71264") @PathVariable String stopAreaId) {
        return networkService.getStopArea(stopAreaId).orElseThrow(() -> notFound("Stop area " + stopAreaId));
    }

    @GetMapping("/stops/{stopAreaId}/departures")
    @Operation(summary = "Next departures of a stop area, real time when available, scheduled otherwise", security = @SecurityRequirement(name = "Auth. Token"))
    public StopDepartures getStopDepartures(@Parameter(example = "IDFM:71264") @PathVariable String stopAreaId,
                                            @Parameter(description = "Only this line", example = "C01371") @RequestParam(required = false) String lineId,
                                            @Parameter(description = "Departures per line and destination, at most 5") @RequestParam(defaultValue = "3") int limit) {
        return departureService.getDepartures(stopAreaId, lineId, limit).orElseThrow(() -> notFound("Stop area " + stopAreaId));
    }

    @GetMapping("/lines/{lineId}")
    @Operation(summary = "Line with its ordered stops and drawn path, per direction and branch", security = @SecurityRequirement(name = "Auth. Token"))
    public LineDetail getLine(@Parameter(example = "C01371") @PathVariable String lineId) {
        return networkService.getLineDetail(lineId).orElseThrow(() -> notFound("Line " + lineId));
    }

    private static void checkPosition(double lat, double lon) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new CustomException("Invalid position", HttpStatus.BAD_REQUEST);
        }
    }

    private static CustomException notFound(String what) {
        return new CustomException(what + " not found", HttpStatus.NOT_FOUND);
    }
}
