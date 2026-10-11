package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import org.apache.hc.client5.http.classic.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.Optional;

/**
 * IGN Géoplateforme geocoder (successor of the BAN's API Adresse): addresses and places (POI index: monuments,
 * airports, parks...), reverse geocoding. Free, no key, 50 requests/s per IP: callers cache. Answers are GeoJSON
 * feature collections. Not PRIM: no quota.
 */
@Component
public class GeoplateformeClient {

    private static final Logger log = LoggerFactory.getLogger(GeoplateformeClient.class);

    /** Typing: a late answer is no use */
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);

    private final ApplicationProperties applicationProperties;

    private final RestTemplate restTemplate;

    public GeoplateformeClient(ApplicationProperties applicationProperties, HttpClient httpClient) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, READ_TIMEOUT));
    }

    /**
     * Addresses and places matching a text being typed, those around [lat], [lon] first when given
     */
    public JsonNode search(String query, Double lat, Double lon, int limit) {
        log.info("Geocoding '{}'", query);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(applicationProperties.getUrls().getGeoplateforme()).path("/search")
            .queryParam("q", query)
            .queryParam("index", "address,poi")
            .queryParam("autocomplete", 1)
            .queryParam("limit", limit)
            .queryParamIfPresent("lat", Optional.ofNullable(lat))
            .queryParamIfPresent("lon", Optional.ofNullable(lon));
        return restTemplate.getForObject(builder.encode().build().toUri(), JsonNode.class);
    }

    /**
     * The address closest to a position (its {@code distance} in meters in the properties)
     */
    public JsonNode reverse(double lat, double lon) {
        log.info("Reverse geocoding");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(applicationProperties.getUrls().getGeoplateforme()).path("/reverse")
            .queryParam("lat", lat)
            .queryParam("lon", lon)
            .queryParam("index", "address")
            .queryParam("limit", 1);
        return restTemplate.getForObject(builder.build().toUri(), JsonNode.class);
    }
}
