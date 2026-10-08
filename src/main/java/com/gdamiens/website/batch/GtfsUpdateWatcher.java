package com.gdamiens.website.batch;

import com.gdamiens.website.model.GtfsImport;
import com.gdamiens.website.repository.GtfsImportRepository;
import com.gdamiens.website.service.GtfsImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Imports the GTFS as soon as IDFM publishes a new version: the dataset's date is checked every 15 minutes (IDFM
 * publishes at 8 h every day, 13 h on working days, and 17 h when SNCF changes its last-minute schedules, e.g. strike
 * days). A version whose import failed is tried again after {@link #RETRY_DELAY}.
 */
@Component
public class GtfsUpdateWatcher {

    private static final Logger log = LoggerFactory.getLogger(GtfsUpdateWatcher.class);

    private static final Duration RETRY_DELAY = Duration.ofHours(2);

    private final GtfsImportService gtfsImportService;

    private final GtfsImportRepository gtfsImportRepository;

    public GtfsUpdateWatcher(GtfsImportService gtfsImportService, GtfsImportRepository gtfsImportRepository) {
        this.gtfsImportService = gtfsImportService;
        this.gtfsImportRepository = gtfsImportRepository;
    }

    /** Imports left running when the application stopped (deployment, crash) */
    @EventListener(ApplicationReadyEvent.class)
    public void closeInterruptedImports() {
        int interrupted = gtfsImportRepository.markInterrupted();
        if (interrupted > 0) {
            log.warn("{} GTFS import(s) interrupted by the last stop", interrupted);
        }
    }

    @Scheduled(initialDelay = 2, fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
    public void importNewVersion() {
        Optional<Instant> published = gtfsImportService.publishedVersion();
        if (published.isEmpty()) {
            return;
        }
        Instant version = published.get();

        boolean alreadyImported = gtfsImportRepository.findLastSuccess()
            .map(GtfsImport::feedDate)
            .filter(Objects::nonNull)
            .filter(imported -> !version.isAfter(imported))
            .isPresent();
        boolean failedRecently = gtfsImportRepository.findLast()
            .filter(last -> GtfsImport.FAILED.equals(last.status()) && version.equals(last.feedDate()))
            .filter(last -> last.startedAt().isAfter(Instant.now().minus(RETRY_DELAY)))
            .isPresent();
        if (alreadyImported || failedRecently) {
            return;
        }

        log.info("New GTFS version published: {}", version);
        gtfsImportService.importGtfs(GtfsImport.WATCH, version);
    }
}
