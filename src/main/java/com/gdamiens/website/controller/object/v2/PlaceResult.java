package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * @param id    stop area id ({@code IDFM:71264}) for {@link Type#STOP_AREA}, Navitia id otherwise
 * @param name  with the town, e.g. {@code Mairie de Montreuil (Montreuil)}
 * @param lines lines of a stop area
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaceResult(Type type, String id, String name, double lat, double lon, List<LineSummary> lines) {

    public enum Type { STOP_AREA, ADDRESS, POI }
}
