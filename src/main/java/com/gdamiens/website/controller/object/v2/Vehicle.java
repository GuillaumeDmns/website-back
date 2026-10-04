package com.gdamiens.website.controller.object.v2;

import java.time.Instant;

/**
 * A vehicle of a line placed between two stops of one of its branches, estimated from the real-time next passages.
 *
 * @param direction    index in {@link LineDetail#directions()}
 * @param branch       index in that direction's branches
 * @param fromStopId   stop area just left, null at the first stop of the branch (waiting to leave)
 * @param toStopId     next stop area
 * @param progress     between the two stops, 0 (just left) to 1 (at the next stop)
 * @param expectedAt   expected time at the next stop
 * @param delaySeconds expected minus scheduled time at the next stop, when known
 */
public record Vehicle(String id, String destination, int direction, int branch, String fromStopId, String toStopId,
                      String toStopName, double progress, Instant expectedAt, Integer delaySeconds) {
}
