package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * First and last scheduled rides of a line between two stop areas over the service day running now (or next, when
 * today's is over), and the first ride of the following service day: what tells the last metro of the night
 *
 * @param first first ride of the service day (may be past)
 * @param last  last ride of the service day (not past)
 * @param next  first ride of the following service day with rides, within a week
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RideSpan(ScheduledRide first, ScheduledRide last, ScheduledRide next) {
}
