package com.gdamiens.website.controller.object.v2;

import com.gdamiens.website.model.Favorite;

/**
 * New favorite. {@code HOME} / {@code WORK} replace the previous one; saving a stop area or a line twice returns the
 * existing favorite.
 *
 * @param stopAreaId required for {@code STOP}, optional for places
 * @param lineId     required for {@code LINE}
 * @param lat        required for {@code HOME}, {@code WORK} and {@code PLACE} (with {@code lon} and {@code label})
 */
public record FavoriteRequest(Favorite.Kind kind, String label, String stopAreaId, String lineId, Double lat, Double lon) {
}
