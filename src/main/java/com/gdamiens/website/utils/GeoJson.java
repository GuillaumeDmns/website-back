package com.gdamiens.website.utils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * GeoJSON produced by PostGIS ({@code ST_AsGeoJSON}).
 */
public final class GeoJson {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private GeoJson() {}

    /**
     * @return {@code [lon, lat]} points of a LineString, empty if there are none
     */
    public static List<double[]> lineStringCoordinates(String geoJson) {
        List<double[]> coordinates = new ArrayList<>();
        if (geoJson == null) {
            return coordinates;
        }
        JsonNode node = JSON_MAPPER.readTree(geoJson).get("coordinates");
        if (node != null) {
            node.forEach(point -> coordinates.add(new double[]{point.get(0).asDouble(), point.get(1).asDouble()}));
        }
        return coordinates;
    }
}
