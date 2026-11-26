package com.gdamiens.website.controller.object.v2;

import com.gdamiens.website.model.TransportMode;

/**
 * @param id          IDFM line id ({@code C01371})
 * @param name        short name shown in the line badge ({@code 1}, {@code A}, {@code N14})
 * @param color       background color, hex without {@code #}
 * @param textColor   text color, hex without {@code #}
 * @param replacement buses replacing a train, metro or tram line during works: their own line in the GTFS
 *                    ({@code C01847} "Remplacement Transilien L" for the train {@code C01740}), same name and color
 */
public record LineSummary(String id, String name, String longName, TransportMode mode, String color, String textColor,
                          boolean replacement) {

    public LineSummary(String id, String name, String longName, TransportMode mode, String color, String textColor) {
        this(id, name, longName, mode, color, textColor, false);
    }
}
