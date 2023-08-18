package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class StationCSV {

    @CsvBindByPosition(position = 0)
    private String stopId;

    @CsvBindByPosition(position = 4)
    private String stopName;

    @CsvBindByPosition(position = 8)
    private String stopType;

    @CsvBindByPosition(position = 16)
    private String gps;

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getStopType() {
        return stopType;
    }

    public void setStopType(String stopType) {
        this.stopType = stopType;
    }

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }
}
