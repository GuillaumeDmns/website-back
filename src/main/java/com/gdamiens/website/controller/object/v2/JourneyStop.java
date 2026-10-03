package com.gdamiens.website.controller.object.v2;

import java.time.Instant;

/**
 * Stop served during a public transport section.
 */
public record JourneyStop(String name, double lat, double lon, Instant time) {
}
