package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * A departure of a line from a stop area, with its arrival at a further stop area
 *
 * @param destination   announced destination
 * @param arrivalAt     arrival at the alighting stop area, when it can be told
 * @param arrivalSource how {@code arrivalAt} is known: {@code realtime} (the vehicle's real-time calls),
 *                      {@code scheduled} (its scheduled trip, shifted by its delay) or {@code typical} (usual ride
 *                      time of the line between the two stops)
 * @param stops         where it stops from the boarding stop area to the alighting one, both included, from its
 *                      scheduled trip (times shifted by its delay): a direct train skips some; null when its trip is
 *                      not known
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ride(Departure departure, String destination, Instant arrivalAt, String arrivalSource, List<JourneyStop> stops) {
}
