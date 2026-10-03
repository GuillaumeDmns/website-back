package com.gdamiens.website.controller.object.v2;

import java.time.Instant;
import java.util.List;

/**
 * A disruption (Navitia / IDFM traffic info).
 *
 * @param title   short title ({@code Métro 13 : Métro en panne - Trains stationnent})
 * @param message plain text details, paragraphs separated by blank lines
 * @param start   start of the current (or next, when not active) application period
 * @param end     end of that period
 * @param active  false for an upcoming disruption
 * @param lineIds IDFM line ids of the impacted lines, empty for a stop-only disruption (elevator...)
 */
public record Disruption(String id, Severity severity, Category category, String title, String message, String cause,
                         Instant start, Instant end, boolean active, Instant updatedAt, List<String> lineIds) {

    public enum Severity { INFO, DISRUPTED, BLOCKING }

    public enum Category { TRAFFIC, WORKS, ELEVATOR }
}
