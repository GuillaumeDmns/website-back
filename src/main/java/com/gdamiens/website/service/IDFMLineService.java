package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.*;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.EstimatedCalls;
import com.gdamiens.website.idfm.EstimatedJourneyVersionFrame;
import com.gdamiens.website.idfm.EstimatedTimetableDelivery;
import com.gdamiens.website.idfm.EstimatedVehicleJourney;
import com.gdamiens.website.idfm.IDFMResponse;
import com.gdamiens.website.idfm.JourneyNote;
import com.gdamiens.website.idfm.ServiceDelivery;
import com.gdamiens.website.idfm.Siri;
import com.gdamiens.website.model.*;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IDFMLineService extends AbstractIDFMService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMLineService.class);

    private final IDFMStopService idfmStopService;

    private final IDFMStopInLineService idfmStopInLineService;

    private final IDFMLineRepository idfmLineRepository;

    private final HttpComponentsClientHttpRequestFactory requestFactory;

    public IDFMLineService(IDFMStopService idfmStopService, IDFMStopInLineService idfmStopInLineService, IDFMLineRepository idfmLineRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmStopService = idfmStopService;
        this.idfmStopInLineService = idfmStopInLineService;
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

    public void refreshStopsInLines() {
        CSVReader<StopAndLineCSV> csvReader = new CSVReader<>(StopAndLineCSV.class);

        Map<String, List<StopAndLineCSV>> stopsInLines = csvReader.readFromUrl(Constants.IDFM_STOPS_IN_LINES_URL, StopAndLineCSV::getRouteId);

        if (stopsInLines == null || stopsInLines.isEmpty()) {
            log.info("No data has been found in the stops in lines CSV file");
            return;
        }

        log.info("Start importing stops in lines pairs");
        log.info("{} stops in lines pairs to import", stopsInLines.size());

        List<IDFMLine> allLines = this.idfmLineRepository.findAll();

        stopsInLines
            .entrySet()
            .parallelStream()
            .forEach(stopInLine -> {
                String lineId = stopInLine.getKey().split(":")[1];

                if (allLines.stream().anyMatch(idfmLine -> idfmLine.getId().equals(lineId))) {
                    try {
                        this.idfmStopInLineService.saveAllStopLine(
                            stopInLine
                                .getValue()
                                .stream()
                                .map(stop -> new IDFMStopInLine(lineId, stop.getStopId()))
                                .collect(Collectors.toList())
                        );
                    }
                    catch (Exception e) {
                        log.error(e.getMessage());
                    }
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
                            return !Arrays.asList("C00563", "C01388").contains(lineCSV.getLineId()); // remove CDG val & orly val
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

    public void updateLineShapes() {
        CSVReader<BusShapesCSV> csvReader = new CSVReader<>(BusShapesCSV.class);

        List<BusShapesCSV> busShapes = csvReader.readFromUrl(Constants.IDFM_BUS_SHAPES_URL);

        if (busShapes == null || busShapes.isEmpty()) {
            log.info("No data has been found in the bus shapes CSV file");
            return;
        }

        log.info("Start importing bus shapes");
        log.info("{} bus shapes to import", busShapes.size());

        busShapes.forEach(busShape -> {
            try {
                this.idfmLineRepository.updateLineShape(busShape.getLineId(), busShape.getShape());
            } catch (Exception e) {
                log.warn("Failed to import bus {} shape : {}", busShape.getLineId(), e.getStackTrace());
            }
        });

        log.info("Finish importing line shapes");
    }

    public IDFMLine getLine(String lineId) {
        return this.idfmLineRepository.findById(lineId).orElse(null);
    }

    public Map<TransportMode, List<LineDTO>> getLinesByTransportMode() {
        return this.idfmLineRepository
            .findAllByOrderByNameAsc()
            .stream()
            .map(LineDTO::new)
            .collect(Collectors.groupingBy(LineDTO::getTransportMode));
    }
}
