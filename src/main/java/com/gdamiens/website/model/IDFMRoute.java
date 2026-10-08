package com.gdamiens.website.model;

/**
 * Line ids: the API exposes IDFM line ids ({@code C01371}) while GTFS route ids are prefixed ({@code IDFM:C01371})
 */
public final class IDFMRoute {

    private IDFMRoute() {
    }

    public static String toRouteId(String lineId) {
        return "IDFM:" + lineId;
    }

    public static String toLineId(String routeId) {
        return routeId.substring(routeId.indexOf(':') + 1);
    }
}
