package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * Traffic state of a line.
 *
 * @param severity worst active disruption, null when the traffic is normal
 * @param titles   titles of the active disruptions, worst first
 * @param works    works going on now on the line or at its stops, and nothing else worse than an information (a
 *                 strike, an incident): what the apps mark with a works sign, like the network's screens
 */
public record LineTraffic(LineSummary line, Disruption.Severity severity, List<String> titles, boolean works) {
}
