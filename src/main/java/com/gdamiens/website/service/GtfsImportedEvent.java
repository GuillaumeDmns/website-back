package com.gdamiens.website.service;

/**
 * Published once a new GTFS replaced the previous one: caches of GTFS data must be dropped (trip, shape and pattern
 * ids change from one feed to the next)
 */
public record GtfsImportedEvent() {
}
