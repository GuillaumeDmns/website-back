package com.gdamiens.website.controller;

import com.gdamiens.website.model.GtfsImport;
import com.gdamiens.website.repository.GtfsImportRepository;
import com.gdamiens.website.client.ApiQuota;
import com.gdamiens.website.client.PrimCircuitBreaker;
import com.gdamiens.website.service.RefreshTokenService;
import com.gdamiens.website.service.StopSearch;
import com.gdamiens.website.service.TrafficService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** State of the service for its administrator: what to watch (supervision, alerts), and what to do by hand */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /** A successful GTFS import older than this is a problem: IDFM publishes at least once a day */
    private static final Duration GTFS_MAX_AGE = Duration.ofHours(26);

    private final ApiQuota apiQuota;

    private final PrimCircuitBreaker circuitBreaker;

    private final GtfsImportRepository gtfsImportRepository;

    private final TrafficService trafficService;

    private final StopSearch stopSearch;

    private final RefreshTokenService refreshTokenService;

    public AdminController(ApiQuota apiQuota, PrimCircuitBreaker circuitBreaker, GtfsImportRepository gtfsImportRepository,
                           TrafficService trafficService, StopSearch stopSearch, RefreshTokenService refreshTokenService) {
        this.apiQuota = apiQuota;
        this.circuitBreaker = circuitBreaker;
        this.gtfsImportRepository = gtfsImportRepository;
        this.trafficService = trafficService;
        this.stopSearch = stopSearch;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * @param lastGtfsSuccess   last successful GTFS import, null if none
     * @param lastGtfsImport    last GTFS import, whatever its outcome
     * @param gtfsStale         no successful import for {@link #GTFS_MAX_AGE}
     * @param trafficSnapshotAt   when the traffic was last fetched (disruptions_bulk, on demand, every 3 min while used),
     *                            null before
     * @param elevatorsSnapshotAt when the elevator status was last fetched (open data, on demand, every hour), null before
     * @param features          PRIM calls of the day per feature, by this server
     */
    public record Status(Instant now, Instant startedAt, List<ApiQuota.Usage> quotas, List<ApiQuota.FeatureUsage> features,
                         List<PrimCircuitBreaker.Status> prim,
                         GtfsImport lastGtfsSuccess, GtfsImport lastGtfsImport, boolean gtfsStale, Instant trafficSnapshotAt,
                         Instant elevatorsSnapshotAt) {
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Service state: PRIM quotas used today (all callers, guests, PRIM's own count), calls per feature, PRIM circuit breakers, GTFS imports, traffic and elevator snapshots",
        security = @SecurityRequirement(name = "Auth. Token"))
    public Status getStatus() {
        Instant now = Instant.now();
        GtfsImport lastSuccess = gtfsImportRepository.findLastSuccess().orElse(null);
        boolean stale = lastSuccess == null || lastSuccess.finishedAt() == null || lastSuccess.finishedAt().plus(GTFS_MAX_AGE).isBefore(now);
        return new Status(now, Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()), apiQuota.usage(),
            apiQuota.featureUsage(), circuitBreaker.status(), lastSuccess, gtfsImportRepository.findLast().orElse(null), stale,
            trafficService.snapshotTime().orElse(null), trafficService.elevatorsSnapshotTime().orElse(null));
    }

    /** What an action did: when, and how much (disruptions fetched, stop areas indexed, sessions deleted...) */
    public record ActionResult(Instant at, Integer count) {
    }

    @PostMapping("/actions/traffic")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetches the traffic now (one disruptions_bulk call) instead of waiting for its 3 min refresh",
        security = @SecurityRequirement(name = "Auth. Token"))
    public ActionResult reloadTraffic() {
        return new ActionResult(trafficService.reloadTraffic(), null);
    }

    @PostMapping("/actions/elevators")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetches the elevator status now (open data) instead of waiting for its hourly refresh",
        security = @SecurityRequirement(name = "Auth. Token"))
    public ActionResult reloadElevators() {
        return new ActionResult(trafficService.reloadElevators(), null);
    }

    @PostMapping("/actions/search-index")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Builds the stop search index again (stop areas, their towns and lines; about 3 s)",
        security = @SecurityRequirement(name = "Auth. Token"))
    public ActionResult rebuildSearchIndex() {
        int stopAreas = stopSearch.rebuild();
        return new ActionResult(Instant.now(), stopAreas);
    }

    @PostMapping("/actions/prim-circuits")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Closes the PRIM circuit breakers: the next calls go through even if PRIM failed lately",
        security = @SecurityRequirement(name = "Auth. Token"))
    public List<PrimCircuitBreaker.Status> resetPrimCircuits() {
        circuitBreaker.reset();
        return circuitBreaker.status();
    }

    @PostMapping("/actions/sessions-cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletes the expired sessions and the ones revoked over 7 days ago now (done every night at 4 h)",
        security = @SecurityRequirement(name = "Auth. Token"))
    public ActionResult cleanUpSessions() {
        int deleted = refreshTokenService.deleteObsoleteTokens();
        return new ActionResult(Instant.now(), deleted);
    }
}
