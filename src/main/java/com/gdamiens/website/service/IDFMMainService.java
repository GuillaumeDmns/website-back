package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.*;
import com.gdamiens.website.idfm.*;
import com.gdamiens.website.model.*;
import com.gdamiens.website.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class IDFMMainService extends AbstractIDFMService {

    private static final Logger log = LoggerFactory.getLogger(IDFMMainService.class);

    public IDFMMainService(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    public String getGTFSlink() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "apikey " + this.getIdfmStaticKey());

        ResponseEntity<GTFSRecords> response = new RestTemplate().exchange(Constants.IDFM_GTFS_URL, HttpMethod.GET, new HttpEntity<>(headers), GTFSRecords.class);

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
