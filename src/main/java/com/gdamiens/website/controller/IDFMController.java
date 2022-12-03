package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.FullIDFMDTO;
import com.gdamiens.website.controller.object.LineCSV;
import com.gdamiens.website.controller.object.NextPassagesStop;
import com.gdamiens.website.controller.object.StationAndLineCSV;
import com.gdamiens.website.controller.object.StationCSV;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.service.CSVReader;
import com.gdamiens.website.service.IDFMLineService;
import com.gdamiens.website.service.IDFMStopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IDFMController {

    private static final Logger log = LoggerFactory.getLogger(IDFMController.class);

    private static final String IDFM_ESTIMATED_TIMETABLE_URL = "https://prim.iledefrance-mobilites.fr/marketplace/estimated-timetable";

    private static final String IDFM_ALL_STATIONS_AND_LINES_URL = "https://data.iledefrance-mobilites.fr/explore/dataset/perimetre-des-donnees-tr-disponibles-plateforme-idfm/download/?format=csv&timezone=Europe/Berlin&lang=fr";

    private static final String IDFM_ALL_STATIONS_URL = "https://data.iledefrance-mobilites.fr/explore/dataset/arrets/download/?format=csv&timezone=Europe/Berlin&lang=fr&use_labels_for_header=true&csv_separator=;";

    private static final String IDFM_ALL_LINES_URL = "https://data.iledefrance-mobilites.fr/explore/dataset/referentiel-des-lignes/download/?format=csv&timezone=Europe/Berlin&lang=fr&use_labels_for_header=true&csv_separator=;";

    private final IDFMLineService idfmLineService;

    private final IDFMStopService idfmStopService;

    public IDFMController(IDFMLineService idfmLineService, IDFMStopService idfmStopService) {
        this.idfmLineService = idfmLineService;
        this.idfmStopService = idfmStopService;
    }


    @GetMapping("/fullIDFM")
    @Operation(summary = "Get full next passages of all IDFM stops by line", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<FullIDFMDTO> getAllStopsByLine(String lineId) {
        try {
            Map<Integer, NextPassagesStop> calls = this.idfmLineService.getAllStopsByLine(lineId, IDFM_ESTIMATED_TIMETABLE_URL);

            IDFMLine idfmLine = this.idfmLineService.getLine(lineId);

            log.info("success requesting IDFM next passages");

            return new ResponseEntity<>(new FullIDFMDTO(idfmLine, calls), HttpStatus.OK);
        } catch (CustomException e) {
            log.info(e.getMessage());

            return new ResponseEntity<>(e.getHttpStatus());
        }
        catch (Exception e) {
            log.info("error during IDFM next passages request");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/update-stations-and-lines")
    @Operation(summary = "Get list of all stops and lines", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateStopsAndLines() {
        try {
            CSVReader<StationAndLineCSV> csvReader = new CSVReader<>(StationAndLineCSV.class);

            Map<String, List<StationAndLineCSV>> linesAndStops = csvReader.readFromUrl(IDFM_ALL_STATIONS_AND_LINES_URL, StationAndLineCSV::getLineRef);

            if (linesAndStops != null && !linesAndStops.isEmpty()) {
                this.idfmLineService.refreshLinesAndStops(linesAndStops);
            } else {
                log.info("No data has been found in the stations & lines CSV file");
            }

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during IDFM update stations & stops request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/update-stops")
    @Operation(summary = "Get list of all stops", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateStopsInformation() {
        try {
            CSVReader<StationCSV> csvReader = new CSVReader<>(StationCSV.class);

            List<StationCSV> stops = csvReader.readFromUrl(IDFM_ALL_STATIONS_URL);

            log.info("{} stops to process", stops.size());

            this.idfmStopService.saveAllStopsFromCSV(stops);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update stops request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/update-lines")
    @Operation(summary = "Get list of all stops", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateLinesInformation() {
        try {
            CSVReader<LineCSV> csvReader = new CSVReader<>(LineCSV.class);

            List<LineCSV> lines = csvReader.readFromUrl(IDFM_ALL_LINES_URL);

            log.info("{} lines to process", lines.size());

            this.idfmLineService.saveAllLinesFromCSV(lines);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update lines request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
