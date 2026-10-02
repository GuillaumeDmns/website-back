package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMStopGtfs;

import java.util.List;

public class StopsByLineDTO {

    private List<IDFMStopGtfs> stops;

    private String shape;

    public StopsByLineDTO(List<IDFMStopGtfs> stops, String shape) {
        this.stops = stops;
        this.shape = shape;
    }

    public List<IDFMStopGtfs> getStops() {
        return stops;
    }

    public void setStops(List<IDFMStopGtfs> stops) {
        this.stops = stops;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }
}
