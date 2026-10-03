package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * @param lines  lines whose name matches the query ({@code 13}, {@code rer b}, {@code bus 38}, {@code T3a})
 * @param places stop areas, addresses and points of interest, best match first
 */
public record SearchResult(List<LineSummary> lines, List<PlaceResult> places) {
}
