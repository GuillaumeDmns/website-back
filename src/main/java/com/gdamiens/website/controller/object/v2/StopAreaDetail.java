package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * @param wheelchairBoarding 1 if every quay is accessible, 2 if none is, 0 otherwise or unknown
 * @param entrances          its ways in and out, by number (empty when the GTFS has none: most stops outside stations)
 */
public record StopAreaDetail(String id, String name, double lat, double lon, short wheelchairBoarding,
                             List<LineSummary> lines, List<Quay> quays, List<Connection> connections, List<Entrance> entrances) {
}
