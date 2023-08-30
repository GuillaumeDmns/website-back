package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class StopAreaCSV extends StopAbstractCSV {

    @CsvBindByPosition(position = 0)
    private String stopAreaId;

    @CsvBindByPosition(position = 4)
    private String name;

    @CsvBindByPosition(position = 5)
    private Double longitude;

    @CsvBindByPosition(position = 6)
    private Double latitude;

    @CsvBindByPosition(position = 8)
    private String type;

    public String getStopAreaId() {
        return stopAreaId;
    }

    public void setStopAreaId(String stopAreaId) {
        this.stopAreaId = stopAreaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
