package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMStop;

import java.util.List;
import java.util.Map;


public class NextPassagesStops {

    private Integer id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String type;

    private Map<String, List<CallGlobal>> nextPassages;

    public NextPassagesStops(IDFMStop idfmStop, Map<String, List<CallGlobal>> calls) {
        this.id = idfmStop.getId();
        this.name = idfmStop.getName();
        this.latitude = idfmStop.getLatitude();
        this.longitude = idfmStop.getLongitude();
        this.type = idfmStop.getType();
        this.nextPassages = calls;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, List<CallGlobal>> getNextPassages() {
        return nextPassages;
    }

    public void setNextPassages(Map<String, List<CallGlobal>> nextPassages) {
        this.nextPassages = nextPassages;
    }
}
