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

    private final IDFMStopAreaService idfmStopAreaService;

    private final IDFMStopLineService idfmStopLineService;

    private final IDFMStopService idfmStopService;

    public IDFMUpdateBatch(IDFMLineService idfmLineService, IDFMOperatorService idfmOperatorService, IDFMStopAreaService idfmStopAreaService, IDFMStopLineService idfmStopLineService, IDFMStopService idfmStopService) {
        this.idfmLineService = idfmLineService;
        this.idfmOperatorService = idfmOperatorService;
        this.idfmStopAreaService = idfmStopAreaService;
        this.idfmStopLineService = idfmStopLineService;
        this.idfmStopService = idfmStopService;
    }

    @Scheduled(cron = "0 3 * * * ?")
    public void launchFullUpdateBatch() {
        log.info("Start cron : Full IDFM update");

        this.idfmStopLineService.truncateTable();
        this.idfmStopService.truncateTable();
        this.idfmLineService.truncateTable();
        this.idfmStopAreaService.truncateTable();
        this.idfmOperatorService.truncateTable();

        this.idfmOperatorService.saveAllOperatorsFromCSV();
        this.idfmStopAreaService.saveAllStopAreasFromCSV();
        this.idfmLineService.saveAllLinesFromCSV();
        this.idfmStopService.saveAllStopsFromCSV();
        this.idfmLineService.refreshLinesAndStops();
        this.idfmStopService.saveStopsRelationsFromCSV();

        log.info("End cron : Full IDFM update");
    }
}
