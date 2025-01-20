package com.gdamiens.website.controller;

import com.gdamiens.website.idfm.navitia.Places;
import com.gdamiens.website.service.IDFMNavitiaService;
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
public class IDFMNavitiaController {

    private static final Logger log = LoggerFactory.getLogger(IDFMNavitiaController.class);

    private final IDFMNavitiaService idfmNavitiaService;

    public IDFMNavitiaController(IDFMNavitiaService idfmNavitiaService) {
        this.idfmNavitiaService = idfmNavitiaService;
    }

    @GetMapping("/places")
    @Operation(summary = "Get list of places for autocomplete", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Places> getLines(String query) {
        try {
            return new ResponseEntity<>(this.idfmNavitiaService.getPlaces(query), HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during IDFM get places");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
