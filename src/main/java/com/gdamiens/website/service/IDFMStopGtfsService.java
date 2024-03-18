package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.model.IDFMStopGtfs;
import com.gdamiens.website.repository.IDFMStopGtfsRepository;
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

    public List<IDFMStopGtfs> getStopsFromLineId(String lineId) {
        return this.idfmStopGtfsRepository.getStopsFromLineId(lineId);
    }

    public List<IDFMStopGtfs> getParentStopsFromLineId(String lineId) {
        return this.idfmStopGtfsRepository.getParentStopsFromLineId(lineId);
    }
}
