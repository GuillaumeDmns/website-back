package com.gdamiens.website.batch;

import com.gdamiens.website.service.GtfsImportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IDFMUpdateBatch {

    private final GtfsImportService gtfsImportService;

    public IDFMUpdateBatch(GtfsImportService gtfsImportService) {
        this.gtfsImportService = gtfsImportService;
    }

    // IDFM publishes the GTFS at 08h (SNCF, every day) and 13h (other operators, working days)
    @Scheduled(cron = "0 30 8 * * *", zone = "Europe/Paris")
    @Scheduled(cron = "0 30 13 * * MON-FRI", zone = "Europe/Paris")
    public void launchGtfsImportBatch() {
        this.gtfsImportService.importGtfs();
    }
}
