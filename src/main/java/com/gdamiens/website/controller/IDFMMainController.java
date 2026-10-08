package com.gdamiens.website.controller;

import com.gdamiens.website.model.GtfsImport;
import com.gdamiens.website.repository.GtfsImportRepository;
import com.gdamiens.website.service.GtfsImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IDFMMainController {

    private final GtfsImportService gtfsImportService;

    private final GtfsImportRepository gtfsImportRepository;

    public IDFMMainController(GtfsImportService gtfsImportService, GtfsImportRepository gtfsImportRepository) {
        this.gtfsImportService = gtfsImportService;
        this.gtfsImportRepository = gtfsImportRepository;
    }

    @PostMapping("/gtfs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Launch the GTFS import in background, whatever the version already imported", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateGTFS() {
        if (!this.gtfsImportService.importGtfsAsync()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/gtfs/imports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Latest GTFS imports, most recent first: version (dataset date), status, rows per table or error",
        security = @SecurityRequirement(name = "Auth. Token"))
    public List<GtfsImport> getImports(@RequestParam(defaultValue = "20") int limit) {
        return this.gtfsImportRepository.findRecent(Math.clamp(limit, 1, 100));
    }
}
