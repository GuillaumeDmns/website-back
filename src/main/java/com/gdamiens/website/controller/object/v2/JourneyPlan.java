package com.gdamiens.website.controller.object.v2;

import java.time.Instant;
import java.util.List;

/**
 * @param journeys options, Navitia order (best first)
 * @param earlier  request it to get earlier options (as an arrival time)
 * @param later    request it to get later options (as a departure time)
 */
public record JourneyPlan(List<JourneyOption> journeys, PageCursor earlier, PageCursor later) {

    public record PageCursor(Instant datetime, boolean arriveBy) {
    }
}
