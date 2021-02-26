package com.gdamiens.website.controller;

import com.gdamiens.website.ratp.wsdl.*;
import com.gdamiens.website.service.RATPService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RATPController {

    private final RATPService ratpService;

    public RATPController(RATPService ratpService) {
        this.ratpService = ratpService;
    }

    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getStations(@RequestParam String search) {
        try {
            List<Station> stationList = ratpService.getStationsByName(search);
            return new ResponseEntity<>(stationList, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/rerA")
    public ResponseEntity<List<Line>> getRerAInfos() {
        try {
            List<Line> lineList = ratpService.getRerAInfos();
            return new ResponseEntity<>(lineList, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/next/{lineId}/{stationName}")
    public ResponseEntity<List<String>> getNext(@PathVariable String lineId, @PathVariable String stationName) {
        try {
            List<Mission> missionList = ratpService.getNext(lineId, stationName);
            List<String> messages = new ArrayList<>();
            missionList.forEach(mission -> {
                messages.add("Direction " + mission.getDirection().getName() + ", prochain train dans " + String.join(", ", mission.getStationsMessages()));
            });
            return new ResponseEntity<>(messages, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}