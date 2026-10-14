package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * A scheduled trip of a line from a stop area to a further one (GTFS)
 *
 * @param departure   scheduled departure from the boarding stop area
 * @param arrival     scheduled arrival at the alighting stop area
 * @param destination the trip's last stop
 * @param mission     SNCF mission code, when given
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScheduledRide(Instant departure, Instant arrival, String destination, String mission) {
}
