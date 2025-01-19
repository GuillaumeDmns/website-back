package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.navitia.Places;
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

import java.util.Optional;

@Service
public class IDFMNavitiaService extends AbstractIDFMService {

    private static final Logger log = LoggerFactory.getLogger(IDFMNavitiaService.class);

    private final RestTemplate restTemplate;

    public IDFMNavitiaService(ApplicationProperties applicationProperties) {
        super(applicationProperties);
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build());
        this.restTemplate = new RestTemplate(requestFactory);

    }

    public void getPlaces(String query) {
        HttpEntity<String> request = this.prepareHttpRequest();


        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_PLACES)
            .queryParam("q", query);

        ResponseEntity<Places> response = this.restTemplate.exchange(uriComponentsBuilder.build().toUri(), HttpMethod.GET, request, Places.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException("IDFM Navitia places response != 200", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Optional<Places> optionalIDFMResponse = Optional.ofNullable(response.getBody());

    }
}
