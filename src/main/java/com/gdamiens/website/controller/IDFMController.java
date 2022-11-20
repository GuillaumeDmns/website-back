package com.gdamiens.website.controller;


import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.idfm.IDFMResponse;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class IDFMController {

    private static final Logger log = LoggerFactory.getLogger(IDFMController.class);

    private static final String IDFM_URL = "https://prim.iledefrance-mobilites.fr/marketplace/estimated-timetable?LineRef=ALL";

    private final HttpComponentsClientHttpRequestFactory requestFactory;

    private final ApplicationProperties applicationProperties;

    public IDFMController(ApplicationProperties applicationProperties) {
        this.requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build());
        this.applicationProperties = applicationProperties;
    }


    @GetMapping("/fullIDFM")
    @Operation(summary = "Get full next passages of all IDFM stops", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<String> fullIDFM() {

        try {
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Accept-encoding", "gzip, deflate");
            headers.set("apiKey", applicationProperties.getIdfmKey());

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<IDFMResponse> response = new RestTemplate(this.requestFactory).exchange(IDFM_URL, HttpMethod.GET, request, IDFMResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                IDFMResponse idfmResponse = response.getBody();

                if (idfmResponse != null) {
                    List<String> stopIds = idfmResponse
                        .getSiri()
                        .getServiceDelivery()
                        .getEstimatedTimetableDelivery()
                        .get(0)
                        .getEstimatedJourneyVersionFrame()
                        .get(0)
                        .getEstimatedVehicleJourney()
                        .stream()
                        .flatMap(evj -> evj.getEstimatedCalls().getEstimatedCall().stream())
                        .map(e -> e.getStopPointRef().getValue())
                        .collect(Collectors.toList());

                    return new ResponseEntity<>(stopIds.get(0), HttpStatus.OK);

                }

                log.info("success requesting IDFM");
            }
        } catch (Exception e) {
            log.info("error during IDFM request");
        }

        return new ResponseEntity<>("ok", HttpStatus.OK);
    }
}
