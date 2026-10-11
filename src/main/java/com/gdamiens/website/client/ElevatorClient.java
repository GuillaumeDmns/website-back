package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import org.apache.hc.client5.http.classic.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

/**
 * Elevator status of the RATP and SNCF stations: open data "État des ascenseurs" on Opendatasoft (not PRIM: no quota),
 * about 1 000 elevators, updated a few times a day. Each one belongs to a stop area ({@code zdcid} 71426 = GTFS
 * {@code IDFM:71426}: all of them match, measured 11 October 2026).
 */
@Component
public class ElevatorClient {

    private static final Logger log = LoggerFactory.getLogger(ElevatorClient.class);

    private final ApplicationProperties applicationProperties;

    private final RestTemplate restTemplate;

    public ElevatorClient(ApplicationProperties applicationProperties, HttpClient httpClient) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.DEFAULT_READ_TIMEOUT));
    }

    /**
     * @return every elevator: {@code zdcid}, {@code liftid}, {@code liftstatus} ({@code available},
     *         {@code notavailable}, {@code unknown}), {@code liftdirection}, {@code liftsituation}, {@code liftstateupdate}…
     */
    public JsonNode getElevators() {
        log.info("Getting the elevator status");
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "apikey " + applicationProperties.getIdfmStaticKey());
        return restTemplate.exchange(applicationProperties.getUrls().getElevators(), HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class)
            .getBody();
    }
}
