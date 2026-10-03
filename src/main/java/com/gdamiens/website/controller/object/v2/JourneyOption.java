package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * One way to go from A to B.
 *
 * @param type            Navitia classification: {@code best}, {@code rapid}, {@code comfort}, {@code less_fallback_walk},
 *                        {@code non_pt_walk}, {@code non_pt_bike}… (the app turns it into a label)
 * @param tags            Navitia tags ({@code less_transfers}, {@code ecologic}…)
 * @param duration        seconds
 * @param walkingDuration seconds of walking
 * @param walkingDistance meters
 * @param co2             grams of CO₂ per passenger
 * @param fare            price in euro cents when known
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JourneyOption(
    String type,
    List<String> tags,
    Instant departure,
    Instant arrival,
    int duration,
    int transfers,
    Integer walkingDuration,
    Integer walkingDistance,
    Double co2,
    Integer fare,
    List<JourneySection> sections) {
}
