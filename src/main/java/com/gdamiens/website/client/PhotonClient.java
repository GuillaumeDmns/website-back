package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import org.apache.hc.client5.http.classic.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.time.Duration;
import java.util.List;

/**
 * Photon geocoder on OpenStreetMap data (komoot's public instance by default, {@code application.urls.photon}): the
 * places the IGN doesn't know, shops, cafés, cinemas, chains (Fnac, Decathlon…), and the closest ones of a kind. Free,
 * no key and no quota, for a fair use: callers cache, and the requests say who sends them. Answers are GeoJSON feature
 * collections with OpenStreetMap's tags ({@code osm_key}, {@code osm_value}). Not PRIM: no quota.
 */
@Component
public class PhotonClient {

    private static final Logger log = LoggerFactory.getLogger(PhotonClient.class);

    /** Typing: a late answer is no use */
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);

    /** Who asks, as the public instance's fair use wants it */
    private static final String USER_AGENT = "guillaumedamiens.com mobility app";

    /** Île-de-France and a little around: west, south, east, north */
    private static final String BOUNDING_BOX = "1.4,48.1,3.6,49.25";

    private final ApplicationProperties applicationProperties;

    private final RestTemplate restTemplate;

    public PhotonClient(ApplicationProperties applicationProperties, HttpClient httpClient) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, READ_TIMEOUT));
    }

    /**
     * Places matching a text being typed in Île-de-France, those around [lat], [lon] first when given
     */
    public JsonNode search(String query, Double lat, Double lon, int limit) {
        log.info("Photon search '{}'", query);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(applicationProperties.getUrls().getPhoton()).path("/api")
            .queryParam("q", query)
            .queryParam("limit", limit)
            .queryParam("lang", "fr")
            .queryParam("bbox", BOUNDING_BOX);
        if (lat != null && lon != null) {
            builder.queryParam("lat", lat).queryParam("lon", lon);
        }
        return get(builder.encode().build().toUri());
    }

    /**
     * The places of a kind closest to a position, within [radiusKm]: OpenStreetMap [tags] ({@code amenity:pharmacy}),
     * any of them
     */
    public JsonNode closest(List<String> tags, double lat, double lon, int limit, double radiusKm) {
        log.info("Photon closest {}", tags);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(applicationProperties.getUrls().getPhoton()).path("/reverse")
            .queryParam("lat", lat)
            .queryParam("lon", lon)
            .queryParam("limit", limit)
            .queryParam("radius", radiusKm)
            .queryParam("lang", "fr");
        tags.forEach(tag -> builder.queryParam("osm_tag", tag));
        return get(builder.encode().build().toUri());
    }

    private JsonNode get(URI uri) {
        return restTemplate.exchange(RequestEntity.get(uri).header(HttpHeaders.USER_AGENT, USER_AGENT).build(), JsonNode.class).getBody();
    }
}
