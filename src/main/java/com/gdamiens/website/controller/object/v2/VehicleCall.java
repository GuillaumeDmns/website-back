package com.gdamiens.website.controller.object.v2;

import java.time.Instant;

/**
 * A next stop of a vehicle
 *
 * @param stopId       stop area
 * @param expectedAt   expected arrival (its departure when no arrival is given)
 * @param departureAt  expected departure, when the feed gives one later than the arrival (a train's stop), else null
 * @param delaySeconds expected minus scheduled time, when known
 * @param platform     platform announced, when known
 */
public record VehicleCall(String stopId, String stopName, Instant expectedAt, Instant departureAt, Integer delaySeconds,
                          String platform) {
}
