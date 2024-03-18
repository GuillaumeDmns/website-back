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

    private final IDFMOperatorService idfmOperatorService;

    private final IDFMUpdateBatch idfmUpdateBatch;

    public IDFMBatchController(IDFMLineService idfmLineService,
                               IDFMOperatorService idfmOperatorService,
                               IDFMUpdateBatch idfmUpdateBatch
    ) {
        this.idfmLineService = idfmLineService;
        this.idfmOperatorService = idfmOperatorService;
        this.idfmUpdateBatch = idfmUpdateBatch;
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
            this.idfmLineService.refreshStopsInLines();

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during IDFM update stations & stops request : {}", e.getMessage());
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
}
