package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.*;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.*;
import com.gdamiens.website.model.*;
import com.gdamiens.website.model.mapper.LineMapper;
import com.gdamiens.website.repository.IDFMLineRepository;
import com.gdamiens.website.utils.Constants;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IDFMMainService extends AbstractIDFMService {

    private static final Logger log = LoggerFactory.getLogger(IDFMMainService.class);

    public IDFMMainService(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    public String getGTFSlink() {
        CSVReader<GTFSCSV> csvReader = new CSVReader<>(GTFSCSV.class);

        List<GTFSCSV> gtfssList = csvReader.readFromUrlWithAuthorization(Constants.IDFM_GTFS_URL, "apikey " + this.getIdfmStaticKey());

        if (gtfssList == null || gtfssList.isEmpty()) {
            log.info("No url in the GTFS");
            return null;
        }

        String gtfsUrl = gtfssList.get(0).getUrl();

        log.info("GTFS URL : {}", gtfsUrl);

        return gtfsUrl;
    }
}
