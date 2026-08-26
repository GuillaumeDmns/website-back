package com.gdamiens.website.service;

import com.gdamiens.website.model.IDFMStopInLine;
import com.gdamiens.website.repository.IDFMStopInLineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IDFMStopInLineService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopInLineService.class);

    private final IDFMStopInLineRepository stopInLineRepository;

    public void truncateTable() {
        log.info("Start truncating stops in lines table");
        
        long start = System.currentTimeMillis();
        this.stopInLineRepository.deleteAllInBatch();
        long end = System.currentTimeMillis();

        log.info("Finish truncating stops in lines table (took {}ms)", end - start);
    }

    public IDFMStopInLineService(IDFMStopInLineRepository stopInLineRepository) {
        this.stopInLineRepository = stopInLineRepository;
    }

    public void saveAllStopLine(List<IDFMStopInLine> stopsInLines) {
        this.stopInLineRepository.saveAll(stopsInLines);
    }
}
