package com.gdamiens.website.controller.object.v2;

import com.gdamiens.website.model.TransportMode;

/**
 * @param id        IDFM line id ({@code C01371})
 * @param name      short name shown in the line badge ({@code 1}, {@code A}, {@code N14})
 * @param color     background color, hex without {@code #}
 * @param textColor text color, hex without {@code #}
 */
public record LineSummary(String id, String name, String longName, TransportMode mode, String color, String textColor) {
}
