package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * @param time      best known departure time (expected when real-time, scheduled otherwise)
 * @param aimedTime scheduled time when known
 * @param realtime  {@code true} if it comes from the real-time feed, {@code false} for the GTFS schedule
 * @param status    real-time status ({@code onTime}, {@code delayed}, {@code cancelled}...)
 * @param atStop    the vehicle is at the stop
 * @param mission     SNCF mission code ({@code POPI}), when given
 * @param trainNumber SNCF train number, when given
 * @param quayId      GTFS quay of a real-time departure ({@code IDFM:12274}) when given; not sent: a stop area's quays
 *                    tell a loop's passes apart in {@code RideService}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Departure(Instant time, Instant aimedTime, boolean realtime, String status, String platform, Boolean atStop, String mission, String trainNumber,
                        @JsonIgnore String quayId) {
}
