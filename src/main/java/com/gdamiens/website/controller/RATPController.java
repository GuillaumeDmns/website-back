package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.LineRequest;
import com.gdamiens.website.controller.object.LinesDTO;
import com.gdamiens.website.controller.object.NextMissionsDTO;
import com.gdamiens.website.controller.object.StationRequest;
import com.gdamiens.website.controller.object.StationsDTO;
import com.gdamiens.website.service.RATPService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RATPController {

    private final RATPService ratpService;

    public RATPController(RATPService ratpService) {
        this.ratpService = ratpService;
    }

    @GetMapping("/stations")
    @ApiOperation(value = "Get list of stations", authorizations = {@Authorization(value = "Auth. Token")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<StationsDTO> getStations(StationRequest stationRequest) {
        try {
            StationsDTO stations = ratpService.getStations(stationRequest.getId(), stationRequest.getName(), stationRequest.getSens(), stationRequest.getLine(), stationRequest.getLimit(), stationRequest.getIsSortedAlpha());
            return new ResponseEntity<>(stations, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/lines")
    @ApiOperation(value = "Get list of lines", authorizations = {@Authorization(value = "Auth. Token")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<LinesDTO> getLines(LineRequest lineRequest) {
        try {
            LinesDTO lines = ratpService.getLinesInfos(lineRequest.getId(), lineRequest.getCode(), lineRequest.getCodeStif(), lineRequest.getRealm(), lineRequest.getReseau());
            return new ResponseEntity<>(lines, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/next/{lineId}/{stationName}")
    @ApiOperation(value = "Get next metros for given line and stations", authorizations = {@Authorization(value = "Auth. Token")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<NextMissionsDTO> getNext(@PathVariable String lineId, @PathVariable String stationName) {
        try {
            NextMissionsDTO nextMissions = ratpService.getNext(lineId, stationName);
            return new ResponseEntity<>(nextMissions, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}