package com.gdamiens.website.controller;

import com.gdamiens.website.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Usage statistics for the administration (website-admin): anonymous counts only */
@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    public AdminStatsController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/accounts")
    @Operation(summary = "Accounts: totals, active and new ones, sign-ups per day over [days], accounts active per day (last 7 days), time since the last sign-in, favorites per account",
        security = @SecurityRequirement(name = "Auth. Token"))
    public AdminStatsService.Accounts getAccounts(@RequestParam(defaultValue = "90") int days) {
        return adminStatsService.accounts(days);
    }

    @GetMapping("/favorites")
    @Operation(summary = "Favorites: per kind, most saved stop areas and lines, added per day over [days]",
        security = @SecurityRequirement(name = "Auth. Token"))
    public AdminStatsService.Favorites getFavorites(@RequestParam(defaultValue = "90") int days) {
        return adminStatsService.favorites(days);
    }

    @GetMapping("/quotas")
    @Operation(summary = "PRIM quotas per API and calls per day and feature over [days] (guests' share included), today's live",
        security = @SecurityRequirement(name = "Auth. Token"))
    public AdminStatsService.Quotas getQuotas(@RequestParam(defaultValue = "30") int days) {
        return adminStatsService.quotas(days);
    }

    @GetMapping("/network")
    @Operation(summary = "GTFS in use: lines per mode, stop areas, quays, service days, published and imported versions; database size and tables",
        security = @SecurityRequirement(name = "Auth. Token"))
    public AdminStatsService.Network getNetwork() {
        return adminStatsService.network();
    }

    @GetMapping("/system")
    @Operation(summary = "JVM and machine: versions, uptime, CPU, memory, threads, garbage collection, free disk",
        security = @SecurityRequirement(name = "Auth. Token"))
    public AdminStatsService.SystemInfo getSystem() {
        return adminStatsService.system();
    }

    @GetMapping("/sites")
    @Operation(summary = "Public pages checked (at most once a minute): HTTP status, response time, TLS certificate expiry",
        security = @SecurityRequirement(name = "Auth. Token"))
    public AdminStatsService.Sites getSites() {
        return adminStatsService.sites();
    }
}
