package com.gdamiens.website.batch;

import com.gdamiens.website.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IDFMUpdateBatch {

    private static final Logger log = LoggerFactory.getLogger(IDFMUpdateBatch.class);

    private final IDFMLineService idfmLineService;

    private final IDFMOperatorService idfmOperatorService;

    private final IDFMStopInLineService idfmStopInLineService;

    private final IDFMStopService idfmStopService;

    public IDFMUpdateBatch(IDFMLineService idfmLineService, IDFMOperatorService idfmOperatorService, IDFMStopInLineService idfmStopInLineService, IDFMStopService idfmStopService) {
        this.idfmLineService = idfmLineService;
        this.idfmOperatorService = idfmOperatorService;
        this.idfmStopInLineService = idfmStopInLineService;
        this.idfmStopService = idfmStopService;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void launchFullUpdateBatch() {
        log.info("Start cron : Full IDFM update");

        this.idfmStopInLineService.truncateTable();
        this.idfmLineService.truncateTable();
        // truncate stop times
        // truncate transfers
        // truncate pathways
        this.idfmStopService.truncateTable();
        // truncate calendar dates
        // truncate calendars
        // truncate trips
        // truncate routes
        // truncate agency
        // truncate transfers

        this.idfmOperatorService.truncateTable();

        this.idfmOperatorService.saveAllOperatorsFromCSV();
        this.idfmLineService.saveAllLinesFromCSV();
        this.idfmLineService.refreshStopsInLines();

        log.info("End cron : Full IDFM update");
    }
}
