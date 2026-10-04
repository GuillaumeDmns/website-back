package com.gdamiens.website.controller.object.v2;

import java.time.Instant;

/**
 * A next stop of a vehicle
 *
 * @param stopId       stop area
 * @param delaySeconds expected minus scheduled time, when known
 * @param platform     platform announced, when known
 */
public record VehicleCall(String stopId, String stopName, Instant expectedAt, Integer delaySeconds, String platform) {
}
