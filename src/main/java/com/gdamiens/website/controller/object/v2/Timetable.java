package com.gdamiens.website.controller.object.v2;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled departures of a line from a stop area over a day, per direction
 *
 * @param date service day (departures after midnight belong to the day before)
 */
public record Timetable(StopAreaSummary stop, LineSummary line, LocalDate date, List<TimetableDirection> directions) {
}
