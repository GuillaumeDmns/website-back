package com.gdamiens.website.controller.object.v2;

import java.util.List;

/**
 * @param name main destinations of the direction, most served first ({@code Saint-Germain-en-Laye / Cergy le Haut})
 */
public record TimetableDirection(String name, List<TimetableEntry> departures) {
}
