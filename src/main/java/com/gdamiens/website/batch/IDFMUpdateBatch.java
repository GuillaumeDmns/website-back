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

    private final IDFMStopInLineService idfmStopInLineService;

    private final IDFMStopAreaInLineService idfmStopAreaInLineService;

    private final IDFMStopService idfmStopService;

    private final IDFMStopOperatorService idfmStopOperatorService;

    private final IDFMRelationsService idfmRelationsService;

    public IDFMUpdateBatch(IDFMLineService idfmLineService, IDFMOperatorService idfmOperatorService, IDFMStopAreaService idfmStopAreaService, IDFMStopInLineService idfmStopInLineService, IDFMStopAreaInLineService idfmStopAreaInLineService, IDFMStopService idfmStopService, IDFMStopOperatorService idfmStopOperatorService, IDFMRelationsService idfmRelationsService) {
        this.idfmLineService = idfmLineService;
        this.idfmOperatorService = idfmOperatorService;
        this.idfmStopAreaService = idfmStopAreaService;
        this.idfmStopInLineService = idfmStopInLineService;
        this.idfmStopAreaInLineService = idfmStopAreaInLineService;
        this.idfmStopService = idfmStopService;
        this.idfmStopOperatorService = idfmStopOperatorService;
        this.idfmRelationsService = idfmRelationsService;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void launchFullUpdateBatch() {
        log.info("Start cron : Full IDFM update");

        this.idfmStopInLineService.truncateTable();
        this.idfmStopAreaInLineService.truncateTable();
        this.idfmStopOperatorService.truncateTable();
        this.idfmStopService.truncateTable();
        this.idfmStopAreaService.truncateTable();
        this.idfmLineService.truncateTable();
        this.idfmOperatorService.truncateTable();

        this.idfmRelationsService.saveRelationsFromCSV();
        this.idfmOperatorService.saveAllOperatorsFromCSV();
        this.idfmLineService.saveAllLinesFromCSV();
        this.idfmStopAreaService.saveAllStopAreasFromCSV();
        this.idfmStopService.saveAllStopsFromCSV();
        this.idfmStopOperatorService.saveAllStopsOperatorFromCSV();
        this.idfmLineService.refreshLinesAndStops();

        log.info("End cron : Full IDFM update");
    }
}
