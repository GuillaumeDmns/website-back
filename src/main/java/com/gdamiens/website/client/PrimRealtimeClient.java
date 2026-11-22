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
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Client of the PRIM real-time APIs (SIRI Lite): stop-monitoring ("Prochains passages, requête unitaire") and
 * estimated-timetable ("requête globale", of a line or of the whole network). Callers cache the results: see {@link ApiQuota} for the daily quotas.
 */
@Component
public class PrimRealtimeClient extends PrimClient {

    /**
     * Reads the SIRI Lite answers. Text tags ({@code DestinationName}, {@code DirectionName}, {@code DestinationDisplay}…)
     * come as a list of one {@code {"value": …}} object, or as that object alone since IDFM's new real-time platform
     * (announced for the end of October 2026): both are read as the list. Its calls already made ({@code RecordedCalls})
     * are read; other unknown tags are kept aside by the generated classes.
     */
    public static final JsonMapper SIRI_JSON = new JacksonJsonHttpMessageConverter().getMapper().rebuild()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .build();

    private static final TypeReference<List<EstimatedVehicleJourney>> JOURNEYS = new TypeReference<>() {
    };

    /** Reads one journey of the answer, the parser then going on to the next one */
    private static final ObjectReader JOURNEY_READER = SIRI_JSON.readerFor(JsonNode.class)
        .without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    private final RestTemplate restTemplate;

    /** The whole network's answer takes a few seconds */
    private final RestTemplate bulkRestTemplate;

    public PrimRealtimeClient(ApplicationProperties applicationProperties, ApiQuota apiQuota, PrimCircuitBreaker circuitBreaker,
                              HttpClient httpClient) {
        super(applicationProperties, apiQuota, circuitBreaker);
        this.restTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.DEFAULT_READ_TIMEOUT));
        this.restTemplate.setMessageConverters(List.of(new JacksonJsonHttpMessageConverter(SIRI_JSON)));
        this.bulkRestTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.BULK_READ_TIMEOUT));
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

    /**
     * Vehicle journeys of the whole network (estimated-timetable with {@code LineRef=ALL}, about 30 MB of JSON, 3 MB
     * gzipped, in November 2026), read as they come and kept as JSON per line: a journey is only turned into objects
     * when its line is asked for ({@link #readJourneys})
     *
     * @return the JSON array of each line's journeys, by IDFM line id ({@code C01371})
     */
    public Map<String, byte[]> getNetworkVehicleJourneys() {
        URI uri = UriComponentsBuilder.fromUriString(primUrl()).path("/estimated-timetable").queryParam("LineRef", "ALL").build().toUri();

        return call(ApiQuota.Feature.VEHICLES, () -> bulkRestTemplate.execute(uri, HttpMethod.GET, bulkRestTemplate.httpEntityCallback(request()),
            response -> new ResponseEntity<>(splitByLine(response.getBody()), response.getHeaders(), response.getStatusCode())));
    }

    /** One line's journeys as kept by {@link #getNetworkVehicleJourneys} */
    public static List<EstimatedVehicleJourney> readJourneys(byte[] json) {
        return SIRI_JSON.readValue(json, JOURNEYS);
    }

    /** The {@code EstimatedVehicleJourney} items of an estimated-timetable answer, grouped by line as JSON arrays */
    static Map<String, byte[]> splitByLine(InputStream body) {
        Map<String, ByteArrayOutputStream> lines = new HashMap<>();
        try (JsonParser parser = SIRI_JSON.createParser(body)) {
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token != JsonToken.PROPERTY_NAME || !"EstimatedVehicleJourney".equals(parser.currentName())) {
                    continue;
                }
                token = parser.nextToken();
                if (token == JsonToken.START_OBJECT) {
                    addJourney(parser, lines);
                } else if (token == JsonToken.START_ARRAY) {
                    while (parser.nextToken() == JsonToken.START_OBJECT) {
                        addJourney(parser, lines);
                    }
                } else {
                    parser.skipChildren();
                }
            }
        }
        Map<String, byte[]> journeys = new HashMap<>();
        lines.forEach((lineId, out) -> {
            out.write(']');
            journeys.put(lineId, out.toByteArray());
        });
        return journeys;
    }

    private static void addJourney(JsonParser parser, Map<String, ByteArrayOutputStream> lines) {
        JsonNode journey = JOURNEY_READER.readValue(parser);
        String lineRef = textValue(journey.get("LineRef"));
        // STIF:Line::C01371:
        String[] parts = lineRef == null ? new String[0] : lineRef.split(":");
        if (parts.length < 4 || parts[3].isEmpty()) {
            return;
        }
        ByteArrayOutputStream out = lines.computeIfAbsent(parts[3], id -> new ByteArrayOutputStream());
        out.write(out.size() == 0 ? '[' : ',');
        out.writeBytes(SIRI_JSON.writeValueAsBytes(journey));
    }

    /** A SIRI text tag: a string, {@code {"value": …}} or a list of it */
    private static String textValue(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isArray()) {
            return node.isEmpty() ? null : textValue(node.get(0));
        }
        if (node.isObject()) {
            return textValue(node.get("value"));
        }
        return node.isString() ? node.asString() : null;
    }

    private Optional<ServiceDelivery> get(ApiQuota.Feature feature, UriComponentsBuilder builder) {
        IDFMResponse response = call(feature,
            () -> restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, request(), IDFMResponse.class));

        return Optional.ofNullable(response)
            .map(IDFMResponse::getSiri)
            .map(Siri::getServiceDelivery);
    }
}
