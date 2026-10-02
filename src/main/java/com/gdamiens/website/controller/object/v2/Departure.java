package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * @param time      best known departure time (expected when real-time, scheduled otherwise)
 * @param aimedTime scheduled time when known
 * @param realtime  {@code true} if it comes from the real-time feed, {@code false} for the GTFS schedule
 * @param status    real-time status ({@code onTime}, {@code delayed}, {@code cancelled}...)
 * @param atStop    the vehicle is at the stop
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Departure(Instant time, Instant aimedTime, boolean realtime, String status, String platform, Boolean atStop) {
}
