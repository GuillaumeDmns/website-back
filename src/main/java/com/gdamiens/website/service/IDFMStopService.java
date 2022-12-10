package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.CallUnit;
import com.gdamiens.website.controller.object.StationCSV;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.IDFMResponse;
import com.gdamiens.website.model.IDFMStop;
import com.gdamiens.website.repository.IDFMStopRepository;
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
import java.util.stream.Collectors;

@Service
public class IDFMStopService extends AbstractIDFMService {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopService.class);

    private final IDFMStopRepository idfmStopRepository;

    private final HttpComponentsClientHttpRequestFactory requestFactory;


    public IDFMStopService(IDFMStopRepository idfmStopRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmStopRepository = idfmStopRepository;
        this.requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build());
    }

    public List<CallUnit> getStopNextPassage(Integer stopId, String url) {
        HttpEntity<String> request = this.prepareHttpRequest();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url)
            .queryParam("MonitoringRef", "STIF:StopPoint:Q:" + stopId + ":");

        ResponseEntity<IDFMResponse> response = new RestTemplate(this.requestFactory).exchange(uriComponentsBuilder.build().toUri(), HttpMethod.GET, request, IDFMResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            IDFMResponse idfmResponse = response.getBody();

            if (idfmResponse != null) {
                return idfmResponse
                    .getSiri()
                    .getServiceDelivery()
                    .getStopMonitoringDelivery()
                    .get(0)
                    .getMonitoredStopVisit()
                    .stream()
                    .map(monitoredStopVisit -> monitoredStopVisit
                        .getMonitoredVehicleJourney()
                        .getMonitoredCall()
                    )
                    .map(CallUnit::new)
                    .collect(Collectors.toList());
            }
        }

        throw new CustomException("IDFM response getNextPassage != 200 or body is null", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public void saveAllStopFromId(List<Integer> stops) {
        this.idfmStopRepository.saveAll(stops.stream().map(IDFMStop::new).collect(Collectors.toList()));
    }

    public void saveAllStopsFromCSV(List<StationCSV> stops) {
        this.idfmStopRepository.saveAll(stops.stream().map(IDFMStop::new).collect(Collectors.toList()));
    }

    public IDFMStop getStop(Integer stopId) {
        return this.idfmStopRepository.findById(stopId).orElse(null);
    }
}
