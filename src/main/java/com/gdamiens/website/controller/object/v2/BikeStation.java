package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * A Vélib station with its availability
 *
 * @param mechanical mechanical bikes available
 * @param electric   electric bikes available
 * @param docks      free docks
 * @param renting    bikes can be taken
 * @param returning  bikes can be returned
 * @param distance   meters from the position asked, when asked around one
 * @param reportedAt last report of the station
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BikeStation(String id, String code, String name, double lat, double lon, int capacity, int mechanical, int electric,
                          int docks, boolean renting, boolean returning, Integer distance, Instant reportedAt) {
}
