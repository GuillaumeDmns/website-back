package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.*;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.EstimatedCall;
import com.gdamiens.website.idfm.EstimatedCalls;
import com.gdamiens.website.idfm.EstimatedJourneyVersionFrame;
import com.gdamiens.website.idfm.EstimatedTimetableDelivery;
import com.gdamiens.website.idfm.EstimatedVehicleJourney;
import com.gdamiens.website.idfm.IDFMResponse;
import com.gdamiens.website.idfm.JourneyNote;
import com.gdamiens.website.idfm.ServiceDelivery;
import com.gdamiens.website.idfm.Siri;
import com.gdamiens.website.model.*;
import com.gdamiens.website.repository.IDFMAgencyRepository;
import com.gdamiens.website.repository.IDFMRouteRepository;
import com.gdamiens.website.repository.IDFMStopGtfsRepository;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class IDFMLineService extends AbstractIDFMService {

    private final IDFMRouteRepository idfmRouteRepository;

    private final IDFMAgencyRepository idfmAgencyRepository;

    private final IDFMStopGtfsRepository idfmStopGtfsRepository;

    private final HttpComponentsClientHttpRequestFactory requestFactory;

    public IDFMLineService(IDFMRouteRepository idfmRouteRepository, IDFMAgencyRepository idfmAgencyRepository, IDFMStopGtfsRepository idfmStopGtfsRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmRouteRepository = idfmRouteRepository;
        this.idfmAgencyRepository = idfmAgencyRepository;
        this.idfmStopGtfsRepository = idfmStopGtfsRepository;
        this.requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build());
    }

    public Map<Integer, NextPassagesStops> getAllStopsByLine(String lineId, String url) {
        List<EstimatedVehicleJourney> estimatedVehicleJourneys = getEstimatedVehicleJourneys(lineId, url);
        return toStopPassages(estimatedVehicleJourneys);
    }

    /**
     * Vehicle journeys of a line with their next calls (PRIM estimated-timetable); all lines when [lineId] is null
     */
    public List<EstimatedVehicleJourney> getEstimatedVehicleJourneys(String lineId, String url) {
        consume(ApiQuota.Api.ESTIMATED_TIMETABLE);
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
        return estimatedVehicleJourneys;
    }

    private Map<Integer, NextPassagesStops> toStopPassages(List<EstimatedVehicleJourney> estimatedVehicleJourneys) {
        Map<Integer, List<EstimatedCall>> callsByStop = estimatedVehicleJourneys
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
            .collect(Collectors.groupingBy(estimatedCall -> Integer.parseInt(estimatedCall.getStopPointRef().getValue().split(":")[3])));

        // SIRI StopPointRef (STIF:StopPoint:Q:<id>:) matches the GTFS stop IDFM:<id>
        Map<Integer, IDFMStopGtfs> stops = this.idfmStopGtfsRepository
            .findAllById(callsByStop.keySet().stream().map(stopId -> "IDFM:" + stopId).toList())
            .stream()
            .collect(Collectors.toMap(stop -> Integer.parseInt(stop.getId().substring("IDFM:".length())), Function.identity()));

        return callsByStop
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> new NextPassagesStops(
                    e.getKey(),
                    stops.get(e.getKey()),
                    e.getValue()
                        .stream()
                        .map(CallGlobal::new)
                        .collect(Collectors.groupingBy(CallGlobal::getDirectionName))
                )
            ));
    }

    public LineDTO getLine(String lineId) {
        return this.idfmRouteRepository.findById(IDFMRoute.toRouteId(lineId))
            .map(route -> new LineDTO(route, this.getTransportMode(route, this.idfmAgencyRepository.findById(route.getAgency_id()).map(IDFMAgency::getName).orElse(null))))
            .orElse(null);
    }

    public String getLineShapeAsGeoJson(String lineId) {
        return this.idfmRouteRepository.getShapeAsGeoJson(IDFMRoute.toRouteId(lineId));
    }

    public Map<TransportMode, List<LineDTO>> getLinesByTransportMode() {
        Map<String, String> agencyNames = this.getAgencyNames();

        return this.idfmRouteRepository
            .findAll()
            .stream()
            .map(route -> new LineDTO(route, this.getTransportMode(route, agencyNames.get(route.getAgency_id()))))
            .sorted(Comparator.comparing(LineDTO::getName, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.groupingBy(LineDTO::getTransportMode));
    }

    private Map<String, String> getAgencyNames() {
        return this.idfmAgencyRepository
            .findAll()
            .stream()
            .collect(Collectors.toMap(IDFMAgency::getId, IDFMAgency::getName));
    }

    private TransportMode getTransportMode(IDFMRoute route, String agencyName) {
        return TransportMode.fromGtfs(route.getType(), agencyName, route.getShort_name());
    }
}
