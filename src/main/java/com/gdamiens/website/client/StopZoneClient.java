package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * IDFM stop zones ("zones de correspondance", open data on Opendatasoft, public, no quota): the town of each GTFS
 * stop area, which the GTFS doesn't give ({@code zdcid} 71264 = {@code IDFM:71264}, town {@code Paris 4e}). About
 * 15 600 zones, 210 KB compressed.
 */
@Component
public class StopZoneClient {

    private static final Logger log = LoggerFactory.getLogger(StopZoneClient.class);

    private final ApplicationProperties applicationProperties;

    private final RestTemplate restTemplate;

    public StopZoneClient(ApplicationProperties applicationProperties, HttpClient httpClient) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.BULK_READ_TIMEOUT));
    }

    /**
     * @return town of each stop area ({@code IDFM:71264} → {@code Paris 4e})
     */
    public Map<String, String> getTowns() {
        log.info("Getting the towns of the stop areas");
        JsonNode zones = restTemplate.getForObject(UriComponentsBuilder.fromUriString(applicationProperties.getUrls().getStopZones())
            .queryParam("select", "zdcid,zdctown").build().toUri(), JsonNode.class);
        Map<String, String> towns = new HashMap<>();
        if (zones != null) {
            zones.values().forEach(zone -> {
                String id = StringUtils.trimToNull(zone.path("zdcid").asString(null));
                String town = StringUtils.trimToNull(zone.path("zdctown").asString(null));
                if (id != null && town != null) {
                    towns.put("IDFM:" + id, town);
                }
            });
        }
        return towns;
    }
}
