package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * A departure of a line from a stop area, with its arrival at a further stop area
 *
 * @param destination   announced destination
 * @param arrivalAt     arrival at the alighting stop area, when it can be told
 * @param arrivalSource how {@code arrivalAt} is known: {@code realtime} (the vehicle's real-time calls),
 *                      {@code scheduled} (its scheduled trip, shifted by its delay) or {@code typical} (usual ride
 *                      time of the line between the two stops)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ride(Departure departure, String destination, Instant arrivalAt, String arrivalSource) {
}
