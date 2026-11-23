package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * Part of a line's path between two consecutive stops, drawn once whatever the branches and directions running it
 *
 * @param fromStopId   stop area it starts from
 * @param toStopId     stop area it ends at
 * @param directionIds directions of the branches running it, from {@code fromStopId} to {@code toStopId} or back
 * @param shape        coordinates {@code [lon, lat]} from {@code fromStopId} to {@code toStopId}
 */
public record LineSection(String fromStopId, String toStopId, List<Integer> directionIds, List<double[]> shape) {
}
