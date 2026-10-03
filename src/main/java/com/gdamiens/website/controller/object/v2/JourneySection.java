package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * @param kind             what happens during the section
 * @param line             public transport line ({@link Kind#TRANSIT} only)
 * @param headsign         direction of the vehicle ({@link Kind#TRANSIT} only)
 * @param boardingPositions best part of the train to board: {@code front}, {@code middle}, {@code back}
 * @param stops            served stops, boarding and alighting included ({@link Kind#TRANSIT} only)
 * @param realtime         times come from the real-time feed
 * @param delay            seconds late compared with the schedule (real time only, negative if early)
 * @param length           meters walked or ridden, when known
 * @param shape            {@code [lon, lat]} points of the path
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record JourneySection(
    Kind kind,
    Instant departure,
    Instant arrival,
    int duration,
    JourneyPoint from,
    JourneyPoint to,
    LineSummary line,
    String headsign,
    List<String> boardingPositions,
    List<JourneyStop> stops,
    List<WalkStep> steps,
    Boolean realtime,
    Integer delay,
    Integer length,
    List<double[]> shape) {

    public enum Kind { WALK, TRANSIT, TRANSFER, WAIT, BIKE, CAR, OTHER }
}
