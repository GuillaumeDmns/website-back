package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * How often a direction leaves over a period of the day, from the schedule
 *
 * @param start      start of the period, the first departure for the first period of the day
 * @param end        end of the period, the last departure for the last period of the day
 * @param departures scheduled departures in the period
 * @param minMinutes usual time between two departures, shortest (absent with a single departure)
 * @param maxMinutes usual time between two departures, longest (the odd longer or shorter gap left out)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Frequency(DayPeriod period, Instant start, Instant end, int departures, Integer minMinutes, Integer maxMinutes) {
}
