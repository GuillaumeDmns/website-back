package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class StopOperatorCSV extends StopAbstractCSV {

    @CsvBindByPosition(position = 0)
    private String stopOperatorId;

    @CsvBindByPosition(position = 6)
    private String name;

    @CsvBindByPosition(position = 7)
    private Double longitude;

    @CsvBindByPosition(position = 8)
    private Double latitude;

    @CsvBindByPosition(position = 9)
    private Integer stopId;

    @CsvBindByPosition(position = 10)
    private String type;

    public String getStopOperatorId() {
        return stopOperatorId;
    }

    public void setStopOperatorId(String stopOperatorId) {
        this.stopOperatorId = stopOperatorId;
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

    public Integer getStopId() {
        return stopId;
    }

    public void setStopId(Integer stopId) {
        this.stopId = stopId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
