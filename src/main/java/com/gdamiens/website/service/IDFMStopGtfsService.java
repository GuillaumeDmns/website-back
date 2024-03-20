package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.CallUnit;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.*;
import com.gdamiens.website.model.IDFMStopGtfs;
import com.gdamiens.website.repository.IDFMStopGtfsRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IDFMStopGtfsService extends AbstractIDFMService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopGtfsService.class);

    private final IDFMStopGtfsRepository idfmStopGtfsRepository;

    private final HttpComponentsClientHttpRequestFactory requestFactory;

    public IDFMStopGtfsService(IDFMStopGtfsRepository idfmStopGtfsRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmStopGtfsRepository = idfmStopGtfsRepository;
        this.requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build());

    }

    @Override
    public void truncateTable() {
        log.info("Start deleting all stops");
        this.idfmStopGtfsRepository.deleteAllInBatch();
        log.info("Finish deleting all stops");
    }

    public List<CallUnit> getStopNextPassage(String stopId, String lineId, String url) {
        HttpEntity<String> request = this.prepareHttpRequest();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url)
            .queryParam("MonitoringRef", "STIF:StopArea:SP:" + stopId.split(":")[1] + ":");

        ResponseEntity<IDFMResponse> response = new RestTemplate(this.requestFactory).exchange(uriComponentsBuilder.build().toUri(), HttpMethod.GET, request, IDFMResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException("IDFM response getNextPassage != 200", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Optional<IDFMResponse> optionalIDFMResponse = Optional.ofNullable(response.getBody());

        List<MonitoredStopVisit> monitoredStopVisits = optionalIDFMResponse
            .map(IDFMResponse::getSiri)
            .map(Siri::getServiceDelivery)
            .map(ServiceDelivery::getStopMonitoringDelivery)
            .filter(l -> !l.isEmpty())
            .map(stopMonitoringDeliveries -> stopMonitoringDeliveries.get(0))
            .map(StopMonitoringDelivery::getMonitoredStopVisit)
            .orElseThrow(() -> new CustomException("IDFM response body does not contain any stop visit", HttpStatus.INTERNAL_SERVER_ERROR));

        return monitoredStopVisits
            .stream()
            .map(CallUnit::new)
            .filter(callUnit -> Optional.ofNullable(callUnit.getLineId())
                .map(callUnitLineId -> lineId == null || callUnitLineId.contains(lineId))
                .orElse(false))
            .collect(Collectors.toList());
    }

    public List<IDFMStopGtfs> getStopsFromLineId(String lineId) {
        return this.idfmStopGtfsRepository.getStopsFromLineId(lineId);
    }

    public List<IDFMStopGtfs> getParentStopsFromLineId(String lineId) {
        return this.idfmStopGtfsRepository.getParentStopsFromLineId(lineId);
    }

    public IDFMStopGtfs getStop(String stopId) {
        return this.idfmStopGtfsRepository.findById(stopId).orElse(null);
    }
}
