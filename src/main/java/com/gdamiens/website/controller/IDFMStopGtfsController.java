package com.gdamiens.website.controller;


import com.gdamiens.website.service.IDFMStopGtfsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IDFMStopGtfsController {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopGtfsController.class);

    private final IDFMStopGtfsService idfmStopGtfsService;

    public IDFMStopGtfsController(IDFMStopGtfsService idfmStopGtfsService) {
        this.idfmStopGtfsService = idfmStopGtfsService;
    }

    @GetMapping("/update-stops-routes")
    @Operation(summary = "Update stops with their associated route", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateStopsRoutes() {
        try {
            this.idfmStopGtfsService.saveStopsRoutesFromCSV();

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during IDFM get stops by lineId request");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
