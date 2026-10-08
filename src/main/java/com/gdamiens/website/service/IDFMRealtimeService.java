package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import com.gdamiens.website.controller.object.CallUnit;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.EstimatedJourneyVersionFrame;
import com.gdamiens.website.idfm.EstimatedTimetableDelivery;
import com.gdamiens.website.idfm.EstimatedVehicleJourney;
import com.gdamiens.website.idfm.IDFMResponse;
import com.gdamiens.website.idfm.MonitoredStopVisit;
import com.gdamiens.website.idfm.ServiceDelivery;
import com.gdamiens.website.idfm.Siri;
import com.gdamiens.website.idfm.StopMonitoringDelivery;
import com.gdamiens.website.utils.Constants;
import org.apache.hc.client5.http.classic.HttpClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Client of the PRIM real-time APIs (SIRI Lite): stop-monitoring ("Prochains passages, requête unitaire") and
 * estimated-timetable ("requête globale"). Callers cache the results: see {@link ApiQuota} for the daily quotas.
 */
@Service
public class IDFMRealtimeService extends AbstractIDFMService {

    private final RestTemplate restTemplate;

    public IDFMRealtimeService(ApplicationProperties applicationProperties, HttpClient httpClient) {
        super(applicationProperties);
        this.restTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.DEFAULT_READ_TIMEOUT));
    }

    /**
     * Next passages at a stop area, every line
     *
     * @param stopAreaId GTFS stop area ({@code IDFM:71264}), i.e. SIRI {@code STIF:StopArea:SP:71264:}
     */
    public List<CallUnit> getStopCalls(String stopAreaId) {
        consume(ApiQuota.Api.STOP_MONITORING);

        String[] splitStopAreaId = stopAreaId.split(":");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_STOP_MONITORING_URL)
            .queryParam("MonitoringRef", "STIF:StopArea:SP:" + splitStopAreaId[splitStopAreaId.length - 1] + ":");

        List<MonitoredStopVisit> monitoredStopVisits = get(builder)
            .map(ServiceDelivery::getStopMonitoringDelivery)
            .filter(deliveries -> !deliveries.isEmpty())
            .map(deliveries -> deliveries.get(0))
            .map(StopMonitoringDelivery::getMonitoredStopVisit)
            .orElseThrow(() -> new CustomException("IDFM response body does not contain any stop visit", HttpStatus.INTERNAL_SERVER_ERROR));

        return monitoredStopVisits.stream()
            .map(CallUnit::new)
            .filter(call -> call.getLineId() != null)
            .toList();
    }

    /**
     * Vehicle journeys of a line with their next calls
     *
     * @param lineId IDFM line id ({@code C01371})
     */
    public List<EstimatedVehicleJourney> getEstimatedVehicleJourneys(String lineId) {
        consume(ApiQuota.Api.ESTIMATED_TIMETABLE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.IDFM_ESTIMATED_TIMETABLE_URL)
            .queryParam("LineRef", "STIF:Line::" + lineId + ":");

        return get(builder)
            .map(ServiceDelivery::getEstimatedTimetableDelivery)
            .filter(deliveries -> !deliveries.isEmpty())
            .map(deliveries -> deliveries.get(0))
            .map(EstimatedTimetableDelivery::getEstimatedJourneyVersionFrame)
            .filter(frames -> !frames.isEmpty())
            .map(frames -> frames.get(0))
            .map(EstimatedJourneyVersionFrame::getEstimatedVehicleJourney)
            .orElseThrow(() -> new CustomException("IDFM response body does not contain any journey", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private Optional<ServiceDelivery> get(UriComponentsBuilder builder) {
        ResponseEntity<IDFMResponse> response = restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, prepareHttpRequest(), IDFMResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException("IDFM real-time response " + response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return Optional.ofNullable(response.getBody())
            .map(IDFMResponse::getSiri)
            .map(Siri::getServiceDelivery);
    }
}
