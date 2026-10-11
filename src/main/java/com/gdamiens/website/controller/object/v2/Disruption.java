package com.gdamiens.website.controller.object.v2;

import java.time.Instant;
import java.util.List;

/**
 * A disruption (IDFM traffic info, or an elevator out of order).
 *
 * @param title    short title ({@code Métro 13 : Métro en panne - Trains stationnent})
 * @param message  plain text details, paragraphs separated by blank lines
 * @param cause    IDFM's cause ({@code TRAVAUX}, {@code PERTURBATION}, {@code INFORMATION}; {@code liftFailure} for an
 *                 elevator)
 * @param start    start of the current (or next, when not active) application period
 * @param end      end of that period
 * @param active   false for an upcoming disruption
 * @param lineIds  IDFM line ids of the impacted lines, empty for a stop-only disruption (elevator, stop not served...)
 * @param sections parts of lines impacted, when the disruption says
 */
public record Disruption(String id, Severity severity, Category category, String title, String message, String cause,
                         Instant start, Instant end, boolean active, Instant updatedAt, List<String> lineIds, List<Section> sections) {

    public enum Severity { INFO, DISRUPTED, BLOCKING }

    public enum Category { TRAFFIC, WORKS, ELEVATOR }

    /**
     * Part of a line between two stop areas ({@code IDFM:71264}), named as in the GTFS
     */
    public record Section(String lineId, String fromId, String fromName, String toId, String toName) {
    }
}
