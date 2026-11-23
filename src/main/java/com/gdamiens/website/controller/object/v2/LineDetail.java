package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * @param sections the line's path between consecutive stops, each part once (both directions on the same tracks or
 *                 street share one): what to draw for the whole line, its branches pointing at theirs
 */
public record LineDetail(LineSummary line, List<LineDirection> directions, List<LineSection> sections) {
}
