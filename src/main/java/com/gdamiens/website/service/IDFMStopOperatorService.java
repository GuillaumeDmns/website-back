package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.*;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.*;
import com.gdamiens.website.model.IDFMStop;
import com.gdamiens.website.model.IDFMStopArea;
import com.gdamiens.website.model.IDFMStopOperator;
import com.gdamiens.website.model.Test;
import com.gdamiens.website.repository.IDFMStopOperatorRepository;
import com.gdamiens.website.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gdamiens.website.utils.GeoUtils.convertLambert94ToLatLong;

@Service
public class IDFMStopOperatorService extends AbstractIDFMService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopOperatorService.class);

    private final IDFMStopOperatorRepository idfmStopOperatorRepository;


    public IDFMStopOperatorService(ApplicationProperties applicationProperties, IDFMStopOperatorRepository idfmStopOperatorRepository) {
        super(applicationProperties);
        this.idfmStopOperatorRepository = idfmStopOperatorRepository;
    }

    public void truncateTable() {
        log.info("Start deleting all stops operator");
        this.idfmStopOperatorRepository.deleteAllInBatch();
        log.info("Finish deleting all stops operator");
    }

    public void saveAllStopsOperatorFromCSV() {
        CSVReader<StopOperatorCSV> csvReader = new CSVReader<>(StopOperatorCSV.class);

        List<StopOperatorCSV> stopsOperator = csvReader.readFromUrl(Constants.IDFM_STOPS_OPERATOR_URL);

        log.info("Start importing stops operator");
        log.info("{} stops operator to process", stopsOperator.size());

        convertLambert94ToLatLong(stopsOperator);

        this.idfmStopOperatorRepository.saveAll(
            stopsOperator.parallelStream().map(IDFMStopOperator::new).collect(Collectors.toList())
        );

        log.info("Finish importing stop areas");
    }

    public void saveStops(List<IDFMStopOperator> stops) {
        this.idfmStopOperatorRepository.saveAll(stops);
    }
}
