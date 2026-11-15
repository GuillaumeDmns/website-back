package com.gdamiens.website.controller.object.v2;

import java.util.List;
import java.util.Map;

/**
 * What the apps are told at start-up.
 *
 * @param minVersions      oldest supported version per platform ({@code android}, {@code web}…), {@code X.Y.Z}
 * @param disabledFeatures features to switch off ({@code vehicles}…)
 * @param notice           message for the home page per language ({@code fr}, {@code en}), empty when there is none
 */
public record AppConfig(Map<String, String> minVersions, List<String> disabledFeatures, Map<String, String> notice) {
}
