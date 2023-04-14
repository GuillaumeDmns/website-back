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
import com.gdamiens.website.utils.Constants;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IDFMLineService extends AbstractIDFMService implements IDFMServiceInterface {

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

    @Override
    public void truncateTable() {
        log.info("Start deleting all lines");
        this.idfmLineRepository.deleteAllInBatch();
        log.info("Finish deleting all lines");
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

    public void refreshLinesAndStops() {
        CSVReader<StationAndLineCSV> csvReader = new CSVReader<>(StationAndLineCSV.class);

        Map<String, List<StationAndLineCSV>> linesAndStops = csvReader.readFromUrl(Constants.IDFM_ALL_STATIONS_AND_LINES_URL, StationAndLineCSV::getLineRef);

        if (linesAndStops == null || linesAndStops.isEmpty()) {
            log.info("No data has been found in the stations & lines CSV file");
            return;
        }

        log.info("Start importing stops-lines pairs");
        log.info("{} stops-lines pairs to import", linesAndStops.size());

        List<IDFMLine> allLines = this.idfmLineRepository.findAll();

        linesAndStops
            .entrySet()
            .parallelStream()
            .filter(lineAndStop -> "STIF".equals(lineAndStop.getKey().split(":")[0]))
            .forEach(lineAndStop -> {
                String lineId = lineAndStop.getKey().split(":")[3];

                if (allLines.stream().anyMatch(idfmLine -> idfmLine.getId().equals(lineId))) {
                    this.idfmStopLineService.saveAllStopLine(
                        lineAndStop
                            .getValue()
                            .stream()
                            .filter(stop -> "STIF".equals(stop.getMonitoringRefZDE().split(":")[0]))
                            .map(stop -> new IDFMStopLine(lineId, Integer.parseInt(stop.getMonitoringRefZDE().split(":")[3])))
                            .collect(Collectors.toList())
                    );
                }
            });
        log.info("Finish importing stops-lines pairs");
    }

    public void saveAllLinesFromCSV() {
        CSVReader<LineCSV> csvReader = new CSVReader<>(LineCSV.class);

        List<LineCSV> lines = csvReader.readFromUrl(Constants.IDFM_ALL_LINES_URL);

        log.info("Start importing lines");
        log.info("{} lines to process", lines.size());

        this.idfmLineRepository.saveAll(
            lines
                .parallelStream()
                .filter(lineCSV -> {
                    switch (lineCSV.getTransportMode()) {
                        case "bus":
                            return lineCSV.getOperatorId() != null && lineCSV.getOperatorId() == 100 && lineCSV.getType().isEmpty();
                        case "rail":
                            return !Arrays.asList("C00563", "C01388").contains(lineCSV.getLineId());
                        case "metro":
                        case "tram":
                            return true;
                        case "funicular":
                        default:
                            return false;
                    }
                })
                .map(LineMapper::csvToDb)
                .collect(Collectors.toList())
        );
        log.info("Finish importing lines");
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
