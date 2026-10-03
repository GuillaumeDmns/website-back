package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * @param directionId GTFS direction_id (0 or 1)
 * @param branches    most frequent first
 */
public record LineDirection(int directionId, List<LineBranch> branches) {
}
