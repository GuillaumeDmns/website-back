package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.CallGlobal;
import com.gdamiens.website.controller.object.LineCSV;
import com.gdamiens.website.controller.object.NextPassagesStops;
import com.gdamiens.website.controller.object.StationAndLineCSV;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.EstimatedCalls;
import com.gdamiens.website.idfm.EstimatedJourneyVersionFrame;
import com.gdamiens.website.idfm.EstimatedTimetableDelivery;
import com.gdamiens.website.idfm.EstimatedVehicleJourney;
import com.gdamiens.website.idfm.IDFMResponse;
import com.gdamiens.website.idfm.JourneyNote;
import com.gdamiens.website.idfm.ServiceDelivery;
import com.gdamiens.website.idfm.Siri;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.IDFMStop;
import com.gdamiens.website.model.IDFMStopLine;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.model.mapper.LineMapper;
import com.gdamiens.website.repository.IDFMLineRepository;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IDFMLineService extends AbstractIDFMService {

    private static final Logger log = LoggerFactory.getLogger(IDFMLineService.class);

    private final IDFMStopService idfmStopService;

    private final IDFMStopLineService idfmStopLineService;

    private final IDFMLineRepository idfmLineRepository;

    private final HttpComponentsClientHttpRequestFactory requestFactory;

    public IDFMLineService(IDFMStopService idfmStopService, IDFMStopLineService idfmStopLineService, IDFMLineRepository idfmLineRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmStopService = idfmStopService;
        this.idfmStopLineService = idfmStopLineService;
        this.idfmLineRepository = idfmLineRepository;
        this.requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build());
    }

    public Map<Integer, NextPassagesStops> getAllStopsByLine(String lineId, String url) {
        HttpEntity<String> request = this.prepareHttpRequest();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url)
            .queryParam("LineRef", lineId != null ? "STIF:Line::" + lineId + ":" : "ALL");

        ResponseEntity<IDFMResponse> response = new RestTemplate(this.requestFactory).exchange(uriComponentsBuilder.build().toUri(), HttpMethod.GET, request, IDFMResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException("IDFM response allStopsByLine != 200", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Optional<IDFMResponse> optionalIDFMResponse = Optional.ofNullable(response.getBody());

        List<EstimatedVehicleJourney> estimatedVehicleJourneys = optionalIDFMResponse
            .map(IDFMResponse::getSiri)
            .map(Siri::getServiceDelivery)
            .map(ServiceDelivery::getEstimatedTimetableDelivery)
            .filter(l -> !l.isEmpty())
            .map(estimatedTimetableDeliveries -> estimatedTimetableDeliveries.get(0))
            .map(EstimatedTimetableDelivery::getEstimatedJourneyVersionFrame)
            .filter(l -> !l.isEmpty())
            .map(estimatedJourneyVersionFrames -> estimatedJourneyVersionFrames.get(0))
            .map(EstimatedJourneyVersionFrame::getEstimatedVehicleJourney)
            .orElseThrow(() -> new CustomException("IDFM response body does not contain any journey", HttpStatus.INTERNAL_SERVER_ERROR));

        return estimatedVehicleJourneys
            .stream()
            .map(Optional::ofNullable)
            .flatMap(estimatedVehicleJourney -> estimatedVehicleJourney
                .map(EstimatedVehicleJourney::getEstimatedCalls)
                .map(EstimatedCalls::getEstimatedCall)
                .orElse(new ArrayList<>())
                .stream()
                .peek(call -> {
                    call.setRecordedAtTime(estimatedVehicleJourney.map(EstimatedVehicleJourney::getRecordedAtTime).orElse(null));
                    call.setJourneyNote(estimatedVehicleJourney
                        .map(EstimatedVehicleJourney::getJourneyNote)
                        .filter(l -> !l.isEmpty())
                        .map(journeyNotes -> journeyNotes.get(0))
                        .map(JourneyNote::getValue)
                        .orElse(null)
                    );
                    call.setFirstOrLastJourney(estimatedVehicleJourney.map(EstimatedVehicleJourney::getFirstOrLastJourney).orElse(null));
                }))
            .filter(estimatedCall -> estimatedCall.getDestinationDisplay() != null && !estimatedCall.getDestinationDisplay().isEmpty())
            .collect(Collectors.groupingBy(estimatedCall -> Integer.parseInt(estimatedCall.getStopPointRef().getValue().split(":")[3])))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    IDFMStop idfmStop = this.idfmStopService.getStop(e.getKey());

                    return new NextPassagesStops(
                        idfmStop != null ? idfmStop : new IDFMStop(),
                        e.getValue()
                            .stream()
                            .map(CallGlobal::new)
                            .collect(Collectors.groupingBy(CallGlobal::getDirectionName))
                    );
                }
            ));
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
        this.idfmLineRepository.saveAll(
            stops
                .parallelStream()
                .filter(lineCSV -> {
                    switch (lineCSV.getTransportMode()) {
                        case "bus":
                            return lineCSV.getOperatorId() != null && lineCSV.getOperatorId() == 100 && lineCSV.getType().isEmpty();
                        case "rail":
                        case "metro":
                        case "tram":
                        case "funicular":
                            return true;
                        default: return false;
                    }
                })
                .map(LineMapper::csvToDb)
                .collect(Collectors.toList())
        );
    }

    public IDFMLine getLine(String lineId) {
        return this.idfmLineRepository.findById(lineId).orElse(null);
    }

    public Map<TransportMode, List<IDFMLine>> getLinesByTransportMode() {
        return this.idfmLineRepository
            .findAllByOrderByNameAsc()
            .stream()
            .collect(Collectors.groupingBy(IDFMLine::getTransportMode));
    }
}
