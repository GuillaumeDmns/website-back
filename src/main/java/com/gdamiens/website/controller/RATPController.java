package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.*;
import com.gdamiens.website.service.RATPService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RATPController {

    private final RATPService ratpService;

    public RATPController(RATPService ratpService) {
        this.ratpService = ratpService;
    }

    @GetMapping("/stations")
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