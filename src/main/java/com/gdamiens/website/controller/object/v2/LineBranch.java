package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * One stop pattern of a line direction. Patterns included in another one (short turns, express services) are merged into it.
 *
 * @param tripCount number of scheduled trips following exactly this pattern
 * @param shape     coordinates {@code [lon, lat]} of the drawn path, empty if the GTFS has none
 */
public record LineBranch(String headsign, int tripCount, List<StopRef> stops, List<double[]> shape) {
}
