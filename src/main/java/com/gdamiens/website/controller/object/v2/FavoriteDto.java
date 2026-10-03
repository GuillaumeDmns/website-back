package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gdamiens.website.model.Favorite;

/**
 * @param label      user-facing name (address, place, stop area name); null for lines
 * @param stopAreaId set for {@code STOP}, and for a place that is a stop area
 * @param stop       stop area with its lines ({@code STOP} only)
 * @param line       line ({@code LINE} only)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FavoriteDto(Long id, Favorite.Kind kind, String label, Double lat, Double lon, String stopAreaId,
                          StopAreaSummary stop, LineSummary line) {
}
