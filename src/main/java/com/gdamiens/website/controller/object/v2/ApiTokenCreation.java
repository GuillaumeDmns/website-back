package com.gdamiens.website.controller.object.v2;

/**
 * @param name     what it is for ("Bruno", "laptop"…)
 * @param validDays days until it expires, 365 when absent (at most 365)
 */
public record ApiTokenCreation(String name, Integer validDays) {
}
