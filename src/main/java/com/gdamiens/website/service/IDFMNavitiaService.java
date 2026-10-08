package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import com.gdamiens.website.exceptions.NavitiaException;
import com.gdamiens.website.utils.Constants;
import org.apache.hc.client5.http.classic.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;

/**
 * Client of the IDFM PRIM Navitia API. Responses are raw snake_case Navitia JSON: the callers only read the fields
 * they need.
 */
@Service
public class IDFMNavitiaService extends AbstractIDFMService {

    private static final Logger log = LoggerFactory.getLogger(IDFMNavitiaService.class);

    private final RestTemplate restTemplate;

    public IDFMNavitiaService(ApplicationProperties applicationProperties, HttpClient httpClient) {
        super(applicationProperties);
        this.restTemplate = new RestTemplate(List.of(new JacksonJsonHttpMessageConverter()));
        this.restTemplate.setRequestFactory(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.DEFAULT_READ_TIMEOUT));
    }

    /**
     * Places matching a text (stop areas, addresses, points of interest...)
     */
    public JsonNode getPlaces(String query, List<String> types, Integer count) {
        log.info("Getting places for query {}", query);
        UriComponentsBuilder builder = navitia("places")
            .queryParam("q", query)
            .queryParamIfPresent("count", Optional.ofNullable(count));
        addAll(builder, "type[]", types);
        return get(builder);
    }

    /**
     * Journey planning with all the options
     *
     * @param from          {@code lon;lat} or a Navitia id ({@code stop_area:IDFM:71264})
     * @param datetime      local Paris time, {@code yyyyMMdd'T'HHmmss}
     * @param walkingSpeed  meters per second
     */
    public JsonNode planJourneys(String from, String to, String datetime, boolean arriveBy, List<String> forbiddenUris,
                                 boolean wheelchair, Double walkingSpeed, Integer maxTransfers) {
        log.info("Planning journeys from {} to {}", from, to);
        UriComponentsBuilder builder = navitia("journeys")
            .queryParam("from", from)
            .queryParam("to", to)
            .queryParam("data_freshness", "realtime")
            .queryParam("min_nb_journeys", 4)
            .queryParam("datetime_represents", arriveBy ? "arrival" : "departure")
            .queryParamIfPresent("datetime", Optional.ofNullable(datetime))
            .queryParamIfPresent("walking_speed", Optional.ofNullable(walkingSpeed))
            .queryParamIfPresent("max_nb_transfers", Optional.ofNullable(maxTransfers));
        if (wheelchair) {
            builder.queryParam("wheelchair", true);
        }
        addAll(builder, "forbidden_uris[]", forbiddenUris);
        return get(builder);
    }

    /**
     * Walking path only (no public transport) between two places. PRIM only computes walking paths: other street
     * modes ({@code bike}, {@code bss}) come back as walking.
     *
     * @param walkingSpeed meters per second
     */
    public JsonNode planWalkingPath(String from, String to, String datetime, Double walkingSpeed) {
        log.info("Planning a walking path from {} to {}", from, to);
        UriComponentsBuilder builder = navitia("journeys")
            .queryParam("from", from)
            .queryParam("to", to)
            .queryParam("direct_path", "only")
            .queryParam("direct_path_mode[]", "walking")
            .queryParamIfPresent("datetime", Optional.ofNullable(datetime))
            .queryParamIfPresent("walking_speed", Optional.ofNullable(walkingSpeed));
        return get(builder);
    }

    /**
     * Every current and upcoming disruption, grouped by network, line and stop area
     */
    public JsonNode getAllTrafficReports() {
        log.info("Getting all traffic reports");
        UriComponentsBuilder builder = navitia("traffic_reports")
            .queryParam("count", 1000)
            .queryParam("depth", 0)
            .queryParam("disable_geojson", true);
        return get(builder);
    }

    private static UriComponentsBuilder navitia(String... pathSegments) {
        return UriComponentsBuilder.fromUriString(Constants.IDFM_NAVITIA_BASE).pathSegment(pathSegments);
    }

    private static void addAll(UriComponentsBuilder builder, String name, List<String> values) {
        if (values != null) {
            values.forEach(value -> builder.queryParam(name, value));
        }
    }

    // Values are percent-encoded (accents, &, = in q...); Navitia errors are rethrown with their status and body
    private JsonNode get(UriComponentsBuilder builder) {
        consume(ApiQuota.Api.NAVITIA);
        try {
            return this.restTemplate.exchange(builder.encode().build().toUri(), HttpMethod.GET, this.prepareHttpRequest(), JsonNode.class).getBody();
        } catch (HttpStatusCodeException e) {
            throw new NavitiaException(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }
}
