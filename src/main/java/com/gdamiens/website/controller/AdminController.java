package com.gdamiens.website.controller;

import com.gdamiens.website.model.GtfsImport;
import com.gdamiens.website.repository.GtfsImportRepository;
import com.gdamiens.website.service.ApiQuota;
import com.gdamiens.website.service.PrimCircuitBreaker;
import com.gdamiens.website.service.TrafficService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** State of the service for its administrator: what to watch (supervision, alerts) */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /** A successful GTFS import older than this is a problem: IDFM publishes at least once a day */
    private static final Duration GTFS_MAX_AGE = Duration.ofHours(26);

    private final ApiQuota apiQuota;

    private final PrimCircuitBreaker circuitBreaker;

    private final GtfsImportRepository gtfsImportRepository;

    private final TrafficService trafficService;

    public AdminController(ApiQuota apiQuota, PrimCircuitBreaker circuitBreaker, GtfsImportRepository gtfsImportRepository,
                           TrafficService trafficService) {
        this.apiQuota = apiQuota;
        this.circuitBreaker = circuitBreaker;
        this.gtfsImportRepository = gtfsImportRepository;
        this.trafficService = trafficService;
    }

    /**
     * @param lastGtfsSuccess   last successful GTFS import, null if none
     * @param lastGtfsImport    last GTFS import, whatever its outcome
     * @param gtfsStale         no successful import for {@link #GTFS_MAX_AGE}
     * @param trafficSnapshotAt when the traffic was last fetched (on demand, every 2 min while used), null before
     */
    public record Status(Instant now, Instant startedAt, List<ApiQuota.Usage> quotas, List<PrimCircuitBreaker.Status> prim,
                         GtfsImport lastGtfsSuccess, GtfsImport lastGtfsImport, boolean gtfsStale, Instant trafficSnapshotAt) {
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Service state: PRIM quotas used today (all callers, guests), PRIM circuit breakers, GTFS imports, traffic snapshot",
        security = @SecurityRequirement(name = "Auth. Token"))
    public Status getStatus() {
        Instant now = Instant.now();
        GtfsImport lastSuccess = gtfsImportRepository.findLastSuccess().orElse(null);
        boolean stale = lastSuccess == null || lastSuccess.finishedAt() == null || lastSuccess.finishedAt().plus(GTFS_MAX_AGE).isBefore(now);
        return new Status(now, Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()), apiQuota.usage(),
            circuitBreaker.status(), lastSuccess, gtfsImportRepository.findLast().orElse(null), stale,
            trafficService.snapshotTime().orElse(null));
    }
}
