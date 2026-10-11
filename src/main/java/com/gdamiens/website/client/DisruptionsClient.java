package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import org.apache.hc.client5.http.classic.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.util.List;

/**
 * Client of PRIM's "Messages Info Trafic, requête globale" ({@code disruptions_bulk}, its own quota): every current and
 * upcoming disruption of the network in one answer (about 1.4 MB, 230 KB compressed, 0.2 s), with the lines and stop
 * points each one impacts. No conditional requests (no ETag nor Last-Modified, measured 11 October 2026).
 */
@Component
public class DisruptionsClient extends PrimClient {

    private static final Logger log = LoggerFactory.getLogger(DisruptionsClient.class);

    private final RestTemplate restTemplate;

    public DisruptionsClient(ApplicationProperties applicationProperties, ApiQuota apiQuota, PrimCircuitBreaker circuitBreaker,
                             HttpClient httpClient) {
        super(applicationProperties, apiQuota, circuitBreaker);
        this.restTemplate = new RestTemplate(List.of(new JacksonJsonHttpMessageConverter()));
        this.restTemplate.setRequestFactory(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.DEFAULT_READ_TIMEOUT));
    }

    /**
     * @return {@code disruptions} (texts, periods, severity), {@code lines} (with the objects impacted by each
     *         disruption: the line, its network or some of its stop points) and {@code lastUpdatedDate}
     */
    public JsonNode getDisruptions() {
        log.info("Getting all disruptions");
        URI uri = UriComponentsBuilder.fromUriString(primUrl()).path("/disruptions_bulk/disruptions/v2").build().toUri();
        return call(ApiQuota.Feature.TRAFFIC, () -> restTemplate.exchange(uri, HttpMethod.GET, request(), JsonNode.class));
    }
}
