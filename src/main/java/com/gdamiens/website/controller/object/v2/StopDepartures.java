package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * @param realtimeAvailable {@code false} when the real-time feed could not be reached (only scheduled departures)
 */
public record StopDepartures(StopAreaSummary stop, boolean realtimeAvailable, List<LineDepartures> lines) {
}
