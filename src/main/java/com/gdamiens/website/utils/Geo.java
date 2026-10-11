package com.gdamiens.website.utils;

/**
 * Distances at city scale
 */
public final class Geo {

    private static final double EARTH_RADIUS = 6_371_000;

    private Geo() {
    }

    /** Meters between two points (equirectangular approximation, enough within the region) */
    public static double meters(double lat1, double lon1, double lat2, double lon2) {
        double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = Math.toRadians(lat2 - lat1);
        return Math.sqrt(x * x + y * y) * EARTH_RADIUS;
    }
}
