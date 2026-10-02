package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * @param wheelchairBoarding GTFS value: 0 unknown, 1 accessible, 2 not accessible
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Quay(String id, String name, double lat, double lon, String platformCode, Short wheelchairBoarding, List<String> lineIds) {
}
