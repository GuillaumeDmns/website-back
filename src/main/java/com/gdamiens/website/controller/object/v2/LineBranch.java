package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * One stop pattern of a line direction. Patterns included in another one (short turns, express services) are merged into it.
 *
 * @param tripCount number of scheduled trips following exactly this pattern
 * @param shape     coordinates {@code [lon, lat]} of its path, made of its {@code sections}
 * @param sections  index in {@link LineDetail#sections()} of the section between each stop and the next one
 */
public record LineBranch(String headsign, int tripCount, List<StopRef> stops, List<double[]> shape, List<Integer> sections) {
}
