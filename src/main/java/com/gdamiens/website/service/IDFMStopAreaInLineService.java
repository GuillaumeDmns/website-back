package com.gdamiens.website.service;

import com.gdamiens.website.model.IDFMStopAreaInLine;
import com.gdamiens.website.repository.IDFMStopAreaInLineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IDFMStopAreaInLineService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopAreaInLineService.class);

    private final IDFMStopAreaInLineRepository stopAreaInLineRepository;

    public void truncateTable() {
        log.info("Start deleting all stop areas in lines");
        this.stopAreaInLineRepository.deleteAllInBatch();
        log.info("Finish deleting all stop areas in lines");
    }

    public IDFMStopAreaInLineService(IDFMStopAreaInLineRepository stopAreaInLineRepository) {
        this.stopAreaInLineRepository = stopAreaInLineRepository;
    }

    public void saveAllStopLine(List<IDFMStopAreaInLine> stopAreasInLines) {
        this.stopAreaInLineRepository.saveAll(stopAreasInLines);
    }
}
