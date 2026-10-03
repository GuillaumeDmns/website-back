package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * Traffic state of a line.
 *
 * @param severity worst active disruption, null when the traffic is normal
 * @param titles   titles of the active disruptions, worst first
 */
public record LineTraffic(LineSummary line, Disruption.Severity severity, List<String> titles) {
}
