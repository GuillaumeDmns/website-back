package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * Next departures of one line towards one destination, soonest first.
 */
public record LineDepartures(LineSummary line, String destination, List<Departure> departures) {
}
