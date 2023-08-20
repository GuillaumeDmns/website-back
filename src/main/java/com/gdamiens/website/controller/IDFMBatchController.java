package com.gdamiens.website.controller;

import com.gdamiens.website.batch.IDFMUpdateBatch;
import com.gdamiens.website.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IDFMBatchController {

    private static final Logger log = LoggerFactory.getLogger(IDFMBatchController.class);

    private final IDFMLineService idfmLineService;

    private final IDFMStopService idfmStopService;

    private final IDFMStopAreaService idfmStopAreaService;

    private final IDFMOperatorService idfmOperatorService;

    private final IDFMUpdateBatch idfmUpdateBatch;

    private final IDFMRelationsService idfmRelationsService;

    public IDFMBatchController(IDFMLineService idfmLineService,
                               IDFMStopService idfmStopService,
                               IDFMStopAreaService idfmStopAreaService,
                               IDFMOperatorService idfmOperatorService,
                               IDFMUpdateBatch idfmUpdateBatch,
                               IDFMRelationsService idfmRelationsService
    ) {
        this.idfmLineService = idfmLineService;
        this.idfmStopService = idfmStopService;
        this.idfmStopAreaService = idfmStopAreaService;
        this.idfmOperatorService = idfmOperatorService;
        this.idfmUpdateBatch = idfmUpdateBatch;
        this.idfmRelationsService = idfmRelationsService;
    }

    @PostMapping("full-db-upgrade")
    @Operation(summary = "Perform a full upgrade of the database entities", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> fullUpgrade() {
        try {
            this.idfmUpdateBatch.launchFullUpdateBatch();

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during IDFM update stations & stops request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update-stops-and-lines")
    @Operation(summary = "Update stops and lines information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateStopsAndLines() {
        try {
            this.idfmLineService.refreshLinesAndStops();

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during IDFM update stations & stops request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update-stops")
    @Operation(summary = "Update stops information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateStopsInformation() {
        try {
            this.idfmStopService.saveAllStopsFromCSV();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update stops request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update-lines")
    @Operation(summary = "Update lines information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateLinesInformation() {
        try {
            this.idfmLineService.saveAllLinesFromCSV();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update lines request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update-stop-areas")
    @Operation(summary = "Update stop areas information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateStopAreas() {
        try {
            this.idfmStopAreaService.saveAllStopAreasFromCSV();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update stop areas request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update-stops-relations")
    @Operation(summary = "Update stops relations information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateStopsRelations() {
        try {
            this.idfmStopService.saveStopsRelationsFromCSV();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update stops relations request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update-operators")
    @Operation(summary = "Update operators information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateOperators() {
        try {
            this.idfmOperatorService.saveAllOperatorsFromCSV();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update operators request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/relations")
    @Operation(summary = "Update relations information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateRelations() {
        try {
            this.idfmRelationsService.saveRelationsFromCSV();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update operators request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
