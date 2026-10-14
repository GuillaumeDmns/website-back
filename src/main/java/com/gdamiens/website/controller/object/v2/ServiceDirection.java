package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * First and last scheduled departures of a direction over a service day, and its frequency per period of the day
 *
 * @param name        main destinations of the direction, most served first ({@code Saint-Germain-en-Laye / Cergy le Haut})
 * @param direction   for a line, index of the direction in its detail ({@code GET /lines/{id}}); absent for a stop area
 * @param at          where the departures are counted, for a line: its stop served by the most trips of the direction
 *                    (its first one when they all are), absent for a stop area (its own departures)
 * @param frequencies periods with departures, in the order of the day
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServiceDirection(String name, Integer direction, StopRef at, Instant first, Instant last, List<Frequency> frequencies) {
}
