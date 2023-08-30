package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class StopCSV extends StopAbstractCSV {

    @CsvBindByPosition(position = 0)
    private String stopId;

    @CsvBindByPosition(position = 4)
    private String stopName;

    @CsvBindByPosition(position = 5)
    private Double longitude;

    @CsvBindByPosition(position = 6)
    private Double latitude;

    @CsvBindByPosition(position = 7)
    private String stopType;

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

    @Override
    public Double getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public Double getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getStopType() {
        return stopType;
    }

    public void setStopType(String stopType) {
        this.stopType = stopType;
    }
}
