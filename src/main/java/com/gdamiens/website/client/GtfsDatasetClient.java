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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * The IDFM GTFS dataset on Opendatasoft (not PRIM: no quota): its version, and its file
 */
@Component
public class GtfsDatasetClient {

    private static final Logger log = LoggerFactory.getLogger(GtfsDatasetClient.class);

    /** Record of the full GTFS among the dataset's files */
    static final String GTFS_FILENAME = "IDFM_gtfs";

    private final ApplicationProperties applicationProperties;

    private final RestTemplate restTemplate;

    private final RestTemplate downloadTemplate;

    public GtfsDatasetClient(ApplicationProperties applicationProperties, HttpClient httpClient) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.DEFAULT_READ_TIMEOUT));
        this.downloadTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.BULK_READ_TIMEOUT));
    }

    /**
     * Version of the GTFS dataset: when Opendatasoft last processed its file (changes with each IDFM publication: 8 h,
     * 13 h on working days, 17 h on disruption days)
     *
     * @return empty when the metadata don't say
     */
    public Optional<Instant> getDataDate() {
        JsonNode dataset = restTemplate.getForObject(datasetUrl(), JsonNode.class);
        JsonNode metas = dataset == null ? null : dataset.path("metas").path("default");
        if (metas == null) {
            return Optional.empty();
        }
        String date = metas.path("data_processed").asString(metas.path("modified").asString(null));
        try {
            return Optional.ofNullable(date).map(Instant::parse);
        } catch (DateTimeParseException e) {
            log.warn("GTFS dataset date not understood: {}", date);
            return Optional.empty();
        }
    }

    /**
     * @return link of the GTFS zip, empty when the dataset has none
     */
    public Optional<String> getFileUrl() {
        JsonNode records = restTemplate.exchange(datasetUrl() + "/records?limit=100", HttpMethod.GET, new HttpEntity<>(authorization()),
            JsonNode.class).getBody();
        Optional<String> url = Optional.ofNullable(records).stream()
            .flatMap(body -> body.path("results").values().stream())
            .filter(record -> GTFS_FILENAME.equals(record.path("filename").asString(null)))
            .map(record -> record.path("url").path("url").asString(null))
            .filter(link -> link != null && !link.isBlank())
            .findFirst();
        log.info("GTFS URL: {}", url.orElse("none"));
        return url;
    }

    /** Downloads the GTFS zip into [target] */
    public void download(String url, Path target) {
        downloadTemplate.execute(url, HttpMethod.GET, request -> request.getHeaders().addAll(authorization()), response -> {
            Files.copy(response.getBody(), target, StandardCopyOption.REPLACE_EXISTING);
            return null;
        });
    }

    private String datasetUrl() {
        return applicationProperties.getUrls().getGtfsDataset();
    }

    private HttpHeaders authorization() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "apikey " + applicationProperties.getIdfmStaticKey());
        return headers;
    }
}
