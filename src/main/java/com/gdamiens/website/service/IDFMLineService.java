package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.Call;
import com.gdamiens.website.controller.object.LineCSV;
import com.gdamiens.website.controller.object.NextPassagesStop;
import com.gdamiens.website.controller.object.StationAndLineCSV;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.IDFMResponse;
import com.gdamiens.website.idfm.JourneyNote;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.IDFMStopLine;
import com.gdamiens.website.repository.IDFMLineRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IDFMLineService {

    private static final Logger log = LoggerFactory.getLogger(IDFMLineService.class);

    private final IDFMStopService idfmStopService;

    private final IDFMStopLineService idfmStopLineService;

    private final IDFMLineRepository idfmLineRepository;

    private final HttpComponentsClientHttpRequestFactory requestFactory;

    private final ApplicationProperties applicationProperties;

    public IDFMLineService(IDFMStopService idfmStopService, IDFMStopLineService idfmStopLineService, IDFMLineRepository idfmLineRepository, ApplicationProperties applicationProperties) {
        this.idfmStopService = idfmStopService;
        this.idfmStopLineService = idfmStopLineService;
        this.idfmLineRepository = idfmLineRepository;
        this.requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build());
        this.applicationProperties = applicationProperties;
    }

    public Map<Integer, NextPassagesStop> getAllStopsByLine(String lineId, String url) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Accept-encoding", "gzip, deflate");
        headers.set("apiKey", applicationProperties.getIdfmKey());

        HttpEntity<String> request = new HttpEntity<>(headers);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url)
            .queryParam("LineRef", lineId != null ? "STIF:Line::" + lineId + ":" : "ALL");

        ResponseEntity<IDFMResponse> response = new RestTemplate(this.requestFactory).exchange(uriComponentsBuilder.build().toUri(), HttpMethod.GET, request, IDFMResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            IDFMResponse idfmResponse = response.getBody();

            if (idfmResponse != null) {

                return idfmResponse
                    .getSiri()
                    .getServiceDelivery()
                    .getEstimatedTimetableDelivery()
                    .get(0)
                    .getEstimatedJourneyVersionFrame()
                    .get(0)
                    .getEstimatedVehicleJourney()
                    .stream()
                    .filter(estimatedVehicleJourney -> estimatedVehicleJourney.getEstimatedCalls() != null && !estimatedVehicleJourney.getEstimatedCalls().getEstimatedCall().isEmpty())
                    .flatMap(estimatedVehicleJourney -> estimatedVehicleJourney
                        .getEstimatedCalls()
                        .getEstimatedCall()
                        .stream()
                        .peek(call -> {
                            call.setRecordedAtTime(estimatedVehicleJourney.getRecordedAtTime());
                            List<JourneyNote> journeyNote = estimatedVehicleJourney.getJourneyNote();
                            call.setJourneyNote(journeyNote != null && !journeyNote.isEmpty() ? journeyNote.get(0).getValue() : null);
                            call.setFirstOrLastJourney(estimatedVehicleJourney.getFirstOrLastJourney());
                        })
                    )
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
            }
        }

        throw new CustomException("IDFM response != 200 or body is null", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public void refreshLinesAndStops(Map<String, List<StationAndLineCSV>> linesAndStops) {
        // Update list of lines
        log.info("Start importing list of lines");
        this.idfmLineRepository.saveAll(
            linesAndStops
            .keySet()
            .parallelStream()
            .filter(line -> "STIF".equals(line.split(":")[0]))
            .map(line -> new IDFMLine(line.split(":")[3]))
            .collect(Collectors.toList())
        );
        log.info("Finished importing list of lines");

        log.info("Start importing list of stops");
        this.idfmStopService.saveAllStopFromId(
            linesAndStops
                .values()
                .parallelStream()
                .flatMap(List::stream)
                .filter(stop -> "STIF".equals(stop.getMonitoringRefZDE().split(":")[0]))
                .map(stop -> Integer.parseInt(stop.getMonitoringRefZDE().split(":")[3]))
                .distinct()
                .collect(Collectors.toList())
        );
        log.info("Finished importing list of stops");

        log.info("Start importing stop/line pairs");
        linesAndStops
            .entrySet()
            .parallelStream()
            .filter(lineAndStop -> "STIF".equals(lineAndStop.getKey().split(":")[0]))
            .forEach(lineAndStop -> {
                String lineId = lineAndStop.getKey().split(":")[3];

                this.idfmStopLineService.saveAllStopLine(
                    lineAndStop
                        .getValue()
                        .stream()
                        .filter(stop -> "STIF".equals(stop.getMonitoringRefZDE().split(":")[0]))
                        .map(stop -> new IDFMStopLine(lineId, Integer.parseInt(stop.getMonitoringRefZDE().split(":")[3])))
                        .collect(Collectors.toList())
                );

        });
        log.info("Finished importing stop/line pairs");
    }

    public void saveAllLinesFromCSV(List<LineCSV> stops) {
        this.idfmLineRepository.saveAll(stops.stream().map(IDFMLine::new).collect(Collectors.toList()));
    }

    public IDFMLine getLine(String lineId) {
        return this.idfmLineRepository.findById(lineId).orElse(null);
    }
}
