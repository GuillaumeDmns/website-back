package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * @param id       stop area id ({@code IDFM:71264}) for {@link Type#STOP_AREA}, else the provider's ({@code osm:N123},
 *                 {@code address:…}, a Navitia id)
 * @param name     with the town, e.g. {@code Mairie de Montreuil (Montreuil)}
 * @param lines    lines of a stop area
 * @param address  a place's street and number, when known ({@code 1-7 Rue Pierre Lescot}): which of the shops of a chain
 * @param category a place's kind, OpenStreetMap's tag value ({@code pharmacy}, {@code cafe}, {@code cinema}…), when known
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaceResult(Type type, String id, String name, double lat, double lon, List<LineSummary> lines, String address,
                          String category) {

    public PlaceResult(Type type, String id, String name, double lat, double lon, List<LineSummary> lines) {
        this(type, id, name, lat, lon, lines, null, null);
    }

    public enum Type { STOP_AREA, ADDRESS, POI }
}
