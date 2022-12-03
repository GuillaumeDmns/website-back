package com.gdamiens.website.controller;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.Call;
import com.gdamiens.website.controller.object.FullIDFMDTO;
import com.gdamiens.website.controller.object.LineCSV;
import com.gdamiens.website.controller.object.NextPassagesStop;
import com.gdamiens.website.controller.object.StationAndLineCSV;
import com.gdamiens.website.controller.object.StationCSV;
import com.gdamiens.website.idfm.IDFMResponse;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.service.CSVReader;
import com.gdamiens.website.service.IDFMLineService;
import com.gdamiens.website.service.IDFMStopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class IDFMController {

    private static final Logger log = LoggerFactory.getLogger(IDFMController.class);

    private static final String IDFM_ESTIMATED_TIMETABLE_URL = "https://prim.iledefrance-mobilites.fr/marketplace/estimated-timetable";

    private static final String IDFM_ALL_STATIONS_AND_LINES_URL = "https://data.iledefrance-mobilites.fr/explore/dataset/perimetre-des-donnees-tr-disponibles-plateforme-idfm/download/?format=csv&timezone=Europe/Berlin&lang=fr";

    private static final String IDFM_ALL_STATIONS_URL = "https://data.iledefrance-mobilites.fr/explore/dataset/arrets/download/?format=csv&timezone=Europe/Berlin&lang=fr&use_labels_for_header=true&csv_separator=;";

    private static final String IDFM_ALL_LINES_URL = "https://data.iledefrance-mobilites.fr/explore/dataset/referentiel-des-lignes/download/?format=csv&timezone=Europe/Berlin&lang=fr&use_labels_for_header=true&csv_separator=;";

    private final HttpComponentsClientHttpRequestFactory requestFactory;

    private final ApplicationProperties applicationProperties;

    private final IDFMLineService idfmLineService;

    private final IDFMStopService idfmStopService;

    public IDFMController(ApplicationProperties applicationProperties, IDFMLineService idfmLineService, IDFMStopService idfmStopService) {
        this.requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build());
        this.applicationProperties = applicationProperties;
        this.idfmLineService = idfmLineService;
        this.idfmStopService = idfmStopService;
    }


    @GetMapping("/fullIDFM")
    @Operation(summary = "Get full next passages of all IDFM stops", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<FullIDFMDTO> fullIDFM(String lineId) {

        try {
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Accept-encoding", "gzip, deflate");
            headers.set("apiKey", applicationProperties.getIdfmKey());

            HttpEntity<String> request = new HttpEntity<>(headers);

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(IDFM_ESTIMATED_TIMETABLE_URL)
                .queryParam("LineRef", lineId != null ? "STIF:Line::" + lineId + ":" : "ALL");

            ResponseEntity<IDFMResponse> response = new RestTemplate(this.requestFactory).exchange(uriComponentsBuilder.build().toUri(), HttpMethod.GET, request, IDFMResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                IDFMResponse idfmResponse = response.getBody();

                if (idfmResponse != null) {
                    IDFMLine idfmLine = this.idfmLineService.getLine(lineId);

                    Map<Integer, NextPassagesStop> calls = idfmResponse
                        .getSiri()
                        .getServiceDelivery()
                        .getEstimatedTimetableDelivery()
                        .get(0)
                        .getEstimatedJourneyVersionFrame()
                        .get(0)
                        .getEstimatedVehicleJourney()
                        .stream()
                        .filter(estimatedVehicleJourney -> estimatedVehicleJourney.getEstimatedCalls() != null && !estimatedVehicleJourney.getEstimatedCalls().getEstimatedCall().isEmpty())
                        .flatMap(estimatedVehicleJourney -> estimatedVehicleJourney.getEstimatedCalls().getEstimatedCall().stream())
                        .filter(estimatedCall -> !estimatedCall.getDestinationDisplay().isEmpty())
                        .collect(Collectors.groupingBy(estimatedCall -> Integer.parseInt(estimatedCall.getStopPointRef().getValue().split(":")[3])))
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> new NextPassagesStop(this.idfmStopService.getStop(e.getKey()), e.getValue()
                                .stream()
                                .map(Call::new)
                                .collect(Collectors.groupingBy(Call::getDirectionName)))
                        ));
//                                    ,this.idfmStopService.getStop(Integer.parseInt(estimatedCall.getStopPointRef().getValue().split(":")[3])))

                    return new ResponseEntity<>(new FullIDFMDTO(idfmLine, calls), HttpStatus.OK);
                }

                log.info("success requesting IDFM");
            }
        } catch (Exception e) {
            log.info("error during IDFM next passages request");
        }

        return new ResponseEntity<>(new FullIDFMDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
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
