package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class StopAndLineCSV {

    @CsvBindByPosition(position = 2)
    private String stopId;

    @CsvBindByPosition(position = 0)
    private String routeId;

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
}
