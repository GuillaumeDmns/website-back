package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A way in and out of a stop area (GTFS entrance, {@code location_type} 2), for the apps' best entrance and exit.
 *
 * @param code               its number on the signs ({@code 7}), when it has one
 * @param name               the street or place it opens on ({@code r. de Presbourg})
 * @param wheelchairBoarding GTFS value: 0 unknown, 1 step-free, 2 not
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Entrance(String id, String code, String name, double lat, double lon, Short wheelchairBoarding) {
}
