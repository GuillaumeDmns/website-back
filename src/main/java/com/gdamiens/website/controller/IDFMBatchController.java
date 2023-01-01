package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.LineCSV;
import com.gdamiens.website.controller.object.OperatorsCSV;
import com.gdamiens.website.controller.object.RelationsCSV;
import com.gdamiens.website.controller.object.StationAndLineCSV;
import com.gdamiens.website.controller.object.StationCSV;
import com.gdamiens.website.controller.object.StopAreaCSV;
import com.gdamiens.website.service.CSVReader;
import com.gdamiens.website.service.IDFMLineService;
import com.gdamiens.website.service.IDFMOperatorService;
import com.gdamiens.website.service.IDFMStopAreaService;
import com.gdamiens.website.service.IDFMStopService;
import com.gdamiens.website.utils.Constants;
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
public class IDFMBatchController {

    private static final Logger log = LoggerFactory.getLogger(IDFMBatchController.class);

    private final IDFMLineService idfmLineService;

    private final IDFMStopService idfmStopService;

    private final IDFMStopAreaService idfmStopAreaService;

    private final IDFMOperatorService idfmOperatorService;

    public IDFMBatchController(IDFMLineService idfmLineService,
                               IDFMStopService idfmStopService,
                               IDFMStopAreaService idfmStopAreaService,
                               IDFMOperatorService idfmOperatorService
    ) {
        this.idfmLineService = idfmLineService;
        this.idfmStopService = idfmStopService;
        this.idfmStopAreaService = idfmStopAreaService;
        this.idfmOperatorService = idfmOperatorService;
    }

    @GetMapping("/update-stops-and-lines")
    @Operation(summary = "Update stops and lines information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateStopsAndLines() {
        try {
            CSVReader<StationAndLineCSV> csvReader = new CSVReader<>(StationAndLineCSV.class);

            Map<String, List<StationAndLineCSV>> linesAndStops = csvReader.readFromUrl(Constants.IDFM_ALL_STATIONS_AND_LINES_URL, StationAndLineCSV::getLineRef);

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
    @Operation(summary = "Update stops information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateStopsInformation() {
        try {
            CSVReader<StationCSV> csvReader = new CSVReader<>(StationCSV.class);

            List<StationCSV> stops = csvReader.readFromUrl(Constants.IDFM_ALL_STATIONS_URL);

            log.info("{} stops to process", stops.size());

            this.idfmStopService.saveAllStopsFromCSV(stops);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update stops request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/update-lines")
    @Operation(summary = "Update lines information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateLinesInformation() {
        try {
            CSVReader<LineCSV> csvReader = new CSVReader<>(LineCSV.class);

            List<LineCSV> lines = csvReader.readFromUrl(Constants.IDFM_ALL_LINES_URL);

            log.info("{} lines to process", lines.size());

            this.idfmLineService.saveAllLinesFromCSV(lines);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update lines request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/update-stop-areas")
    @Operation(summary = "Update stop areas information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateStopAreas() {
        try {
            CSVReader<StopAreaCSV> csvReader = new CSVReader<>(StopAreaCSV.class);

            List<StopAreaCSV> stopAreas = csvReader.readFromUrl(Constants.IDFM_STOP_AREAS_URL);

            log.info("{} stop areas to process", stopAreas.size());

            this.idfmStopAreaService.saveAllStopAreasFromCSV(stopAreas);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update stop areas request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/update-stops-relations")
    @Operation(summary = "Update stops relations information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateStopsRelations() {
        try {
            CSVReader<RelationsCSV> csvReader = new CSVReader<>(RelationsCSV.class);

            List<RelationsCSV> relations = csvReader.readFromUrl(Constants.IDFM_RELATIONS_URL);

            log.info("{} relations to process", relations.size());

            this.idfmStopService.saveStopsRelationsFromCSV(relations);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update stops relations request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/update-operators")
    @Operation(summary = "Update operators information", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateOperators() {
        try {
            CSVReader<OperatorsCSV> csvReader = new CSVReader<>(OperatorsCSV.class);

            List<OperatorsCSV> operators = csvReader.readFromUrl(Constants.IDFM_OPERATORS_URL);

            log.info("{} operators to process", operators.size());

            this.idfmOperatorService.saveAllOperatorsFromCSV(operators);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.info("error during IDFM update operators request : {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
