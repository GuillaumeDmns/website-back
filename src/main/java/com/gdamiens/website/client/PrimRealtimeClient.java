package com.gdamiens.website.client;

import com.gdamiens.website.client.siri.EstimatedJourneyVersionFrame;
import com.gdamiens.website.client.siri.EstimatedTimetableDelivery;
import com.gdamiens.website.client.siri.EstimatedVehicleJourney;
import com.gdamiens.website.client.siri.IDFMResponse;
import com.gdamiens.website.client.siri.ServiceDelivery;
import com.gdamiens.website.client.siri.Siri;
import com.gdamiens.website.client.siri.StopMonitoringDelivery;
import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import com.gdamiens.website.exceptions.CustomException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Client of the PRIM real-time APIs (SIRI Lite): stop-monitoring ("Prochains passages, requête unitaire") and
 * estimated-timetable ("requête globale"). Callers cache the results: see {@link ApiQuota} for the daily quotas.
 */
@Component
public class PrimRealtimeClient extends PrimClient {

    /**
     * Reads the SIRI Lite answers. Text tags ({@code DestinationName}, {@code DirectionName}, {@code DestinationDisplay}…)
     * come as a list of one {@code {"value": …}} object, or as that object alone since IDFM's new real-time platform
     * (announced for the end of October 2026): both are read as the list. Unknown tags ({@code RecordedCalls}) are kept
     * aside by the generated classes.
     */
    public static final JsonMapper SIRI_JSON = new JacksonJsonHttpMessageConverter().getMapper().rebuild()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .build();

    private final RestTemplate restTemplate;

    public PrimRealtimeClient(ApplicationProperties applicationProperties, ApiQuota apiQuota, PrimCircuitBreaker circuitBreaker,
                              HttpClient httpClient) {
        super(applicationProperties, apiQuota, circuitBreaker);
        this.restTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.DEFAULT_READ_TIMEOUT));
        this.restTemplate.setMessageConverters(List.of(new JacksonJsonHttpMessageConverter(SIRI_JSON)));
    }

    /**
     * Next passages at a stop area, every line
     *
     * @param stopAreaId GTFS stop area ({@code IDFM:71264}), i.e. SIRI {@code STIF:StopArea:SP:71264:}
     */
    public List<StopCall> getStopCalls(String stopAreaId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(primUrl()).path("/stop-monitoring")
            .queryParam("MonitoringRef", "STIF:StopArea:SP:" + stopAreaId.substring(stopAreaId.lastIndexOf(':') + 1) + ":");

        return get(ApiQuota.Feature.DEPARTURES, builder)
            .map(ServiceDelivery::getStopMonitoringDelivery)
            .filter(deliveries -> !deliveries.isEmpty())
            .map(deliveries -> deliveries.getFirst())
            .map(StopMonitoringDelivery::getMonitoredStopVisit)
            .orElseThrow(() -> new CustomException("IDFM response body does not contain any stop visit", HttpStatus.INTERNAL_SERVER_ERROR))
            .stream()
            .map(StopCall::from)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Vehicle journeys of a line with their next calls
     *
     * @param lineId IDFM line id ({@code C01371})
     */
    public List<EstimatedVehicleJourney> getEstimatedVehicleJourneys(String lineId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(primUrl()).path("/estimated-timetable")
            .queryParam("LineRef", "STIF:Line::" + lineId + ":");

        return get(ApiQuota.Feature.VEHICLES, builder)
            .map(ServiceDelivery::getEstimatedTimetableDelivery)
            .filter(deliveries -> !deliveries.isEmpty())
            .map(deliveries -> deliveries.getFirst())
            .map(EstimatedTimetableDelivery::getEstimatedJourneyVersionFrame)
            .filter(frames -> !frames.isEmpty())
            .map(frames -> frames.getFirst())
            .map(EstimatedJourneyVersionFrame::getEstimatedVehicleJourney)
            .orElseThrow(() -> new CustomException("IDFM response body does not contain any journey", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private Optional<ServiceDelivery> get(ApiQuota.Feature feature, UriComponentsBuilder builder) {
        IDFMResponse response = call(feature,
            () -> restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request(), IDFMResponse.class));

        return Optional.ofNullable(response)
            .map(IDFMResponse::getSiri)
            .map(Siri::getServiceDelivery);
    }
}
