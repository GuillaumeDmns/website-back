package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.FullMissionDTO;
import com.gdamiens.website.controller.object.LinesDTO;
import com.gdamiens.website.controller.object.NextMissionsDTO;
import com.gdamiens.website.controller.object.ReseauxDTO;
import com.gdamiens.website.controller.object.StationsDTO;
import com.gdamiens.website.service.RATPLineService;
import com.gdamiens.website.service.RATPMissionService;
import com.gdamiens.website.service.RATPReseauService;
import com.gdamiens.website.service.RATPStationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RATPController {

    private final RATPLineService ratpLineService;
    private final RATPStationService ratpStationService;
    private final RATPMissionService ratpMissionService;
    private final RATPReseauService ratpReseauService;

    public RATPController(RATPLineService ratpLineService,
                          RATPStationService ratpStationService,
                          RATPMissionService ratpMissionService,
                          RATPReseauService ratpReseauService) {
        this.ratpLineService = ratpLineService;
        this.ratpStationService = ratpStationService;
        this.ratpMissionService = ratpMissionService;
        this.ratpReseauService = ratpReseauService;
    }

//    @GetMapping("/stations")
//    @ApiOperation(value = "Get list of stations", authorizations = {@Authorization(value = "Auth. Token")})
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<StationsDTO> getStations(StationRequest stationRequest) {
//        try {
//            StationsDTO stations = ratpStationService.getStations(stationRequest.getId(), stationRequest.getName(), stationRequest.getSens(), stationRequest.getLine(), stationRequest.getLimit(), stationRequest.getIsSortedAlpha());
//            return new ResponseEntity<>(stations, HttpStatus.OK);
//        }
//        catch (Exception e) {
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @GetMapping("/refresh-lines")
    @ApiOperation(value = "Refresh lines & reseau", authorizations = {@Authorization(value = "Auth. Token")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> refreshDbLines() {
        try {
            ratpLineService.refreshLines(false);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/reseaux")
    @ApiOperation(value = "Get list of reseaux", authorizations = {@Authorization(value = "Auth. Token")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReseauxDTO> getReseaux() {
        try {
            ReseauxDTO reseauxDTO = this.ratpReseauService.getReseaux();
            return new ResponseEntity<>(reseauxDTO, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/lines")
    @ApiOperation(value = "Get list of lines by reseau", authorizations = {@Authorization(value = "Auth. Token")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<LinesDTO> getLines(String reseauId) {
        try {
            LinesDTO lines = ratpLineService.getLinesByReseauId(reseauId);
            return new ResponseEntity<>(lines, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stations")
    @ApiOperation(value = "Get list of stations by line and by stationName", authorizations = {@Authorization(value = "Auth. Token")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<StationsDTO> getStations(String lineId, String stationName) {
        try {
            StationsDTO stationsDTO = ratpStationService.getStations(lineId, stationName);
            return new ResponseEntity<>(stationsDTO, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/next")
    @ApiOperation(value = "Get next metros for given line and station", authorizations = {@Authorization(value = "Auth. Token")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<NextMissionsDTO> getNext(String lineId, String stationId) {
        try {
            NextMissionsDTO nextMissions = ratpMissionService.getNext(lineId, stationId);
            return new ResponseEntity<>(nextMissions, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/full-mission")
    @ApiOperation(value = "Get full mission for given line", authorizations = {@Authorization(value = "Auth. Token")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<FullMissionDTO> getFullMission(String lineId) {
        try {
            FullMissionDTO fullMission = ratpMissionService.getFullMission(lineId);
            return new ResponseEntity<>(fullMission, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}