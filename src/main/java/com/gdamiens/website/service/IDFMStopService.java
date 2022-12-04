package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.StationCSV;
import com.gdamiens.website.model.IDFMStop;
import com.gdamiens.website.repository.IDFMStopRepository;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

    public void getStopNextPassage(Integer stopId, String url) {
        HttpEntity<String> request = this.prepareHttpRequest();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url)
            .queryParam("MonitoringRef", "STIF:StopPoint:Q:" + stopId + ":");

        ResponseEntity<Object> response = new RestTemplate(this.requestFactory).exchange(uriComponentsBuilder.build().toUri(), HttpMethod.GET, request, Object.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("request successful");
        }
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
