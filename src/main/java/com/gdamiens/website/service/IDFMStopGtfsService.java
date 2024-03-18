package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.StopAndLineCSV;
import com.gdamiens.website.model.IDFMStopGtfs;
import com.gdamiens.website.repository.IDFMStopGtfsRepository;
import com.gdamiens.website.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IDFMStopGtfsService extends AbstractIDFMService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopGtfsService.class);

    private final IDFMStopGtfsRepository idfmStopGtfsRepository;

    public IDFMStopGtfsService(IDFMStopGtfsRepository idfmStopGtfsRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmStopGtfsRepository = idfmStopGtfsRepository;
    }

    @Override
    public void truncateTable() {
        log.info("Start deleting all stops");
        this.idfmStopGtfsRepository.deleteAllInBatch();
        log.info("Finish deleting all stops");
    }

    public void saveStopsRoutesFromCSV() {
        CSVReader<StopAndLineCSV> csvReader = new CSVReader<>(StopAndLineCSV.class);

        List<StopAndLineCSV> stopsList = csvReader.readFromUrl(Constants.IDFM_STOPS_IN_LINES_URL);


        log.info("Start setting gtfs stops route id");
        log.info("{} relations to process", stopsList.size());

        stopsList
            .parallelStream()
            .forEach(stop -> this.idfmStopGtfsRepository.updateRouteId(stop.getStopId(), stop.getRouteId()));

        log.info("Finish setting gtfs stops route id");
    }

    public List<IDFMStopGtfs> getStopAreasFromLineId(String lineId) {
        return this.idfmStopGtfsRepository.getStopsFromLineId(lineId);
    }
}
