package com.gdamiens.website.controller.object.v2;

import java.time.LocalDate;
import java.util.List;

/**
 * Service hours of a line (at a stop area, or along the line) over a service day, per direction
 *
 * @param date service day (departures after midnight belong to the day before)
 */
public record LineServiceHours(LineSummary line, LocalDate date, List<ServiceDirection> directions) {
}
