package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMStopGtfs;

import java.util.List;
import java.util.Map;


public class NextPassagesStops {

    private Integer id;
    private String name;
    private Double latitude;
    private Double longitude;

    private Map<String, List<CallGlobal>> nextPassages;

    public NextPassagesStops(Integer id, IDFMStopGtfs stop, Map<String, List<CallGlobal>> calls) {
        this.id = id;
        if (stop != null) {
            this.name = stop.getName();
            this.latitude = stop.getLatitude();
            this.longitude = stop.getLongitude();
        }
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

    public Map<String, List<CallGlobal>> getNextPassages() {
        return nextPassages;
    }

    public void setNextPassages(Map<String, List<CallGlobal>> nextPassages) {
        this.nextPassages = nextPassages;
    }
}
