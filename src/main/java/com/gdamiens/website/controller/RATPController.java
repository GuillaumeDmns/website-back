package com.gdamiens.website.controller;

import com.gdamiens.website.ratp.wsdl.*;
import com.gdamiens.website.service.RATPService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RATPController {

    private final RATPService ratpService;

    public RATPController(RATPService ratpService) {
        this.ratpService = ratpService;
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
    public ResponseEntity<List<Mission>> getNext(@PathVariable String lineId, @PathVariable String stationName) {
        try {
            List<Mission> missionList = ratpService.getNext(lineId, stationName);
            return new ResponseEntity<>(missionList, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}