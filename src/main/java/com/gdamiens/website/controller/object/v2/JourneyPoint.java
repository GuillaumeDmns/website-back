package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Start or end of a journey section.
 *
 * @param stopAreaId stop area ({@code IDFM:71264}) when the point is a stop
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JourneyPoint(String name, double lat, double lon, String stopAreaId) {
}
