package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class LineCSV {

    @CsvBindByPosition(position = 0)
    private String stopId;

    @CsvBindByPosition(position = 2)
    private String name;

    @CsvBindByPosition(position = 4)
    private String transportMode;

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }
}
