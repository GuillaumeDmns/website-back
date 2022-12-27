package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class LineCSV {

    @CsvBindByPosition(position = 0)
    private String lineId;

    @CsvBindByPosition(position = 2)
    private String name;

    @CsvBindByPosition(position = 6)
    private String transportMode;

    @CsvBindByPosition(position = 8)
    private String type;

    @CsvBindByPosition(position = 9)
    private Integer operatorId;

    @CsvBindByPosition(position = 12)
    private String networkName;

    @CsvBindByPosition(position = 13)
    private String lineIdBackgroundColor;

    @CsvBindByPosition(position = 14)
    private String lineIdColor;

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getLineIdBackgroundColor() {
        return lineIdBackgroundColor;
    }

    public void setLineIdBackgroundColor(String lineIdBackgroundColor) {
        this.lineIdBackgroundColor = lineIdBackgroundColor;
    }

    public String getLineIdColor() {
        return lineIdColor;
    }

    public void setLineIdColor(String lineIdColor) {
        this.lineIdColor = lineIdColor;
    }
}
