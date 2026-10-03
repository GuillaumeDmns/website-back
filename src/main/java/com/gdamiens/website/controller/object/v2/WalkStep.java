package com.gdamiens.website.controller.object.v2;

/**
 * Walking instruction, e.g. "Tournez à gauche sur Rue de Rivoli".
 *
 * @param length   meters
 * @param duration seconds
 */
public record WalkStep(String instruction, int length, int duration) {
}
