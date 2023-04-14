package com.gdamiens.website.service;

import com.gdamiens.website.model.IDFMStopLine;
import com.gdamiens.website.repository.IDFMStopLineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IDFMStopLineService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopLineService.class);

    private final IDFMStopLineRepository stopLineRepository;

    public void truncateTable() {
        log.info("Start deleting all stops-lines");
        this.stopLineRepository.deleteAllInBatch();
        log.info("Finish deleting all stops-lines");
    }

    public IDFMStopLineService(IDFMStopLineRepository stopLineRepository) {
        this.stopLineRepository = stopLineRepository;
    }

    public void saveAllStopLine(List<IDFMStopLine> stopline) {
        this.stopLineRepository.saveAll(stopline);
    }
}
