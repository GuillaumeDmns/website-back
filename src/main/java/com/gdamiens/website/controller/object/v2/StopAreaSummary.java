package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * A stop area (GTFS parent station, {@code IDFM:71264}): the unit used for real-time departures.
 *
 * @param distance meters from the requested position, only on nearby results
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StopAreaSummary(String id, String name, double lat, double lon, Integer distance, List<LineSummary> lines) {
}
