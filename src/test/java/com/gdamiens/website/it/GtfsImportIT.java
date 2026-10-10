package com.gdamiens.website.it;

import com.gdamiens.website.batch.GtfsUpdateWatcher;
import com.gdamiens.website.model.GtfsImport;
import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import com.gdamiens.website.support.MiniGtfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/** The GTFS import (download, COPY, checks, schema swap, cache invalidation) and the watch of new versions */
class GtfsImportIT extends IntegrationTest {

    @Autowired
    private GtfsUpdateWatcher watcher;

    /** Leaves the usual network for the other tests */
    @AfterEach
    void restoreNetwork() {
        if (!GtfsImport.SUCCESS.equals(gtfsImportRepository.findLast().map(GtfsImport::status).orElse(null))
            || !GTFS_VERSION.equals(gtfsImportRepository.findLast().map(GtfsImport::feedDate).orElse(null))) {
            serveGtfs(MiniGtfs.network(), GTFS_VERSION);
            gtfsImportService.importGtfs(GtfsImport.MANUAL, GTFS_VERSION);
        }
    }

    @Test
    void recordsTheImportedNetwork() {
        Api.Response imports = admin().api().get("/api/gtfs/imports?limit=5");

        assertThat(imports.status()).isEqualTo(200);
        JsonNode last = imports.json().path(0);
        assertThat(last.path("status").asString()).isEqualTo("SUCCESS");
        assertThat(last.path("rowCounts").asString()).contains("routes=4", "agency=3", "stops=20", "calendar=1", "transfers=2");
    }

    @Test
    void aNewImportReplacesTheNetworkAndItsCaches() {
        Api user = newUser().api();
        assertThat(user.get("/api/v2/lines/C01371").json().path("line").path("longName").asString()).isEqualTo("Métro 1");

        Instant version = GTFS_VERSION.plus(Duration.ofHours(1));
        serveGtfs(MiniGtfs.network(files -> {
            files.put("routes.txt", files.get("routes.txt").replace("Métro 1", "Métro 1 nouveau"));
            return files;
        }), version);
        gtfsImportService.importGtfs(GtfsImport.MANUAL, version);

        assertThat(gtfsImportRepository.findLast()).get().satisfies(last -> {
            assertThat(last.status()).isEqualTo(GtfsImport.SUCCESS);
            assertThat(last.feedDate()).isEqualTo(version);
        });
        assertThat(EXTERNAL.requests("/gtfs/files/IDFM_gtfs.zip")).singleElement()
            .satisfies(request -> assertThat(request.header("Authorization")).isEqualTo("apikey test-opendatasoft-key"));
        assertThat(user.get("/api/v2/lines/C01371").json().path("line").path("longName").asString()).isEqualTo("Métro 1 nouveau");
    }

    @Test
    void refusesATruncatedFeedAndKeepsTheNetwork() {
        Instant version = GTFS_VERSION.plus(Duration.ofHours(2));
        serveGtfs(MiniGtfs.busesOnly(), version);

        gtfsImportService.importGtfs(GtfsImport.MANUAL, version);

        assertThat(gtfsImportRepository.findLast()).get().satisfies(last -> {
            assertThat(last.status()).isEqualTo(GtfsImport.FAILED);
            assertThat(last.error()).contains("looks truncated");
        });
        assertThat(newUser().api().get("/api/v2/lines/C01742").status()).isEqualTo(200);
    }

    @Test
    void refusesUnknownColumns() {
        Instant version = GTFS_VERSION.plus(Duration.ofHours(3));
        serveGtfs(MiniGtfs.network(files -> {
            files.put("agency.txt", files.get("agency.txt").replaceFirst("\n", ",agency_mascot\n"));
            return files;
        }), version);

        gtfsImportService.importGtfs(GtfsImport.MANUAL, version);

        assertThat(gtfsImportRepository.findLast()).get().satisfies(last -> {
            assertThat(last.status()).isEqualTo(GtfsImport.FAILED);
            assertThat(last.error()).contains("unknown columns [agency_mascot]");
        });
    }

    @Test
    void theWatcherImportsANewVersionOnce() {
        long before = gtfsImportRepository.findRecent(100).size();

        // Same version as the imported one: nothing to do
        watcher.importNewVersion();
        assertThat(gtfsImportRepository.findRecent(100)).hasSize((int) before);

        Instant version = GTFS_VERSION.plus(Duration.ofHours(4));
        serveGtfs(MiniGtfs.network(), version);
        watcher.importNewVersion();
        watcher.importNewVersion();

        assertThat(gtfsImportRepository.findRecent(100)).hasSize((int) before + 1);
        assertThat(gtfsImportRepository.findLast()).get().satisfies(last -> {
            assertThat(last.source()).isEqualTo(GtfsImport.WATCH);
            assertThat(last.feedDate()).isEqualTo(version);
            assertThat(last.status()).isEqualTo(GtfsImport.SUCCESS);
        });
    }

    @Test
    void aFailedVersionIsNotRetriedAtOnce() {
        Instant version = GTFS_VERSION.plus(Duration.ofHours(5));
        serveGtfs(MiniGtfs.busesOnly(), version);
        watcher.importNewVersion();
        long after = gtfsImportRepository.findRecent(100).size();

        watcher.importNewVersion();

        assertThat(gtfsImportRepository.findRecent(100)).hasSize((int) after);
    }

    @Test
    void onlyAdminsTriggerAnImport() {
        assertThat(newUser().api().post("/api/gtfs").status()).isEqualTo(403);
        assertThat(guest().post("/api/gtfs").status()).isEqualTo(403);
        assertThat(newUser().api().get("/api/gtfs/imports").status()).isEqualTo(403);
    }
}
