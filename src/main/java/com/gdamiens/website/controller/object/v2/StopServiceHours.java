package com.gdamiens.website.controller.object.v2;

import java.time.LocalDate;
import java.util.List;

/**
 * Service hours of the lines leaving a stop area over a service day
 *
 * @param date  service day (departures after midnight belong to the day before)
 * @param lines lines with departures from the stop area that day, in the stop area's order
 */
public record StopServiceHours(StopAreaSummary stop, LocalDate date, List<LineServiceHours> lines) {
}
