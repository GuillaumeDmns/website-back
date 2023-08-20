package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class RelationsCSV {

    @CsvBindByPosition(position = 4)
    private Integer stopAreaId;

    @CsvBindByPosition(position = 5)
    private String stopAreaVersion;

    @CsvBindByPosition(position = 6)
    private Integer stopId;

    @CsvBindByPosition(position = 7)
    private String stopVersion;

    @CsvBindByPosition(position = 8)
    private Integer stopOperatorId;

    @CsvBindByPosition(position = 9)
    private String stopOperatorVersion;

    public Integer getStopAreaId() {
        return stopAreaId;
    }

    public void setStopAreaId(Integer stopAreaId) {
        this.stopAreaId = stopAreaId;
    }

    public String getStopAreaVersion() {
        return stopAreaVersion;
    }

    public void setStopAreaVersion(String stopAreaVersion) {
        this.stopAreaVersion = stopAreaVersion;
    }

    public Integer getStopId() {
        return stopId;
    }

    public void setStopId(Integer stopId) {
        this.stopId = stopId;
    }

    public String getStopVersion() {
        return stopVersion;
    }

    public void setStopVersion(String stopVersion) {
        this.stopVersion = stopVersion;
    }

    public Integer getStopOperatorId() {
        return stopOperatorId;
    }

    public void setStopOperatorId(Integer stopOperatorId) {
        this.stopOperatorId = stopOperatorId;
    }

    public String getStopOperatorVersion() {
        return stopOperatorVersion;
    }

    public void setStopOperatorVersion(String stopOperatorVersion) {
        this.stopOperatorVersion = stopOperatorVersion;
    }
}
