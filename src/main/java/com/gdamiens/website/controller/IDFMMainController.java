package com.gdamiens.website.controller;

import com.gdamiens.website.service.GtfsImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IDFMMainController {

    private final GtfsImportService gtfsImportService;

    public IDFMMainController(GtfsImportService gtfsImportService) {
        this.gtfsImportService = gtfsImportService;
    }

    @PostMapping("/gtfs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Launch the GTFS import in background", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateGTFS() {
        if (!this.gtfsImportService.importGtfsAsync()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
