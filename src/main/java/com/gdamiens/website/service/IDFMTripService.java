package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.gtfs.TripsCSV;
import com.gdamiens.website.model.IDFMTrip;
import com.gdamiens.website.repository.IDFMTripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class IDFMTripService extends AbstractIDFMService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMTripService.class);

    private final IDFMTripRepository idfmTripRepository;

    public IDFMTripService(IDFMTripRepository idfmTripRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmTripRepository = idfmTripRepository;
    }

    public void truncateTable() {
        log.info("Start deleting all trips");
        this.idfmTripRepository.deleteAllInBatch();
        log.info("Finish deleting all trips");
    }

    public void saveAllTripsFromTripsCSVList(List<TripsCSV> tripsCSVList) {
        log.info("Start importing trips");
        log.info("{} trips to process", tripsCSVList.size());

        this.idfmTripRepository.saveAll(
            tripsCSVList.parallelStream().map(IDFMTrip::new).collect(Collectors.toList())
        );

        log.info("Finish importing trips");
    }
}
