package com.gdamiens.website.controller.object.v2;

/**
 * Walking instruction, e.g. "Tournez à gauche sur Rue de Rivoli".
 *
 * @param length    meters
 * @param duration  seconds
 * @param direction turn at its start, in degrees: 0 straight on, negative to the left, positive to the right (Navitia)
 * @param offset    meters from the start of the section where it starts, for turn-by-turn guidance along the path
 * @param name      street, when given
 */
public record WalkStep(String instruction, int length, int duration, int direction, int offset, String name) {
}
