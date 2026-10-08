package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import com.gdamiens.website.controller.object.GTFSRecords;
import com.gdamiens.website.utils.Constants;
import org.apache.hc.client5.http.classic.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class IDFMMainService extends AbstractIDFMService {

    private static final Logger log = LoggerFactory.getLogger(IDFMMainService.class);

    private final RestTemplate restTemplate;

    public IDFMMainService(ApplicationProperties applicationProperties, HttpClient httpClient) {
        super(applicationProperties);
        this.restTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.DEFAULT_READ_TIMEOUT));
    }

    /**
     * Version of the GTFS dataset: when Opendatasoft last processed its file (changes with each IDFM publication: 8 h,
     * 13 h on working days, 17 h on disruption days)
     *
     * @return empty when the metadata don't say
     */
    public Optional<Instant> getGtfsDataDate() {
        JsonNode dataset = this.restTemplate.getForObject(Constants.IDFM_GTFS_DATASET_URL, JsonNode.class);
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

    public String getGTFSlink() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "apikey " + this.getIdfmStaticKey());

        ResponseEntity<GTFSRecords> response = this.restTemplate.exchange(Constants.IDFM_GTFS_URL, HttpMethod.GET, new HttpEntity<>(headers), GTFSRecords.class);

        String gtfsUrl = Optional.ofNullable(response.getBody())
            .map(GTFSRecords::getResults)
            .orElse(List.of())
            .stream()
            .filter(gtfsRecord -> Constants.IDFM_GTFS_FILENAME.equals(gtfsRecord.getFilename()))
            .map(GTFSRecords.GTFSRecord::getUrl)
            .filter(Objects::nonNull)
            .map(GTFSRecords.GTFSFile::getUrl)
            .findFirst()
            .orElse(null);

        if (gtfsUrl == null) {
            log.info("No {} url in the GTFS dataset", Constants.IDFM_GTFS_FILENAME);
            return null;
        }

        log.info("GTFS URL : {}", gtfsUrl);

        return gtfsUrl;
    }
}
