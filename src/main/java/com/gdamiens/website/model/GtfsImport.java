package com.gdamiens.website.model;

import java.time.Instant;

/**
 * A GTFS import ({@code public.gtfs_import})
 *
 * @param source    {@code WATCH} (new dataset version detected) or {@code MANUAL} ({@code POST /api/gtfs})
 * @param feedDate  processing date of the imported dataset version on Opendatasoft, null when unknown
 * @param status    {@code RUNNING}, {@code SUCCESS} or {@code FAILED}
 * @param rowCounts rows loaded per table, e.g. {@code agency=80, calendar=...}
 */
public record GtfsImport(long id, String source, Instant feedDate, Instant startedAt, Instant finishedAt, String status,
                         String rowCounts, String error) {

    public static final String WATCH = "WATCH";
    public static final String MANUAL = "MANUAL";

    public static final String RUNNING = "RUNNING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
}
