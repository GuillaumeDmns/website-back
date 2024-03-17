package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMStopGtfs;

import java.util.List;

public class StopsByLineDTO {

    private List<IDFMStopGtfs> stops;

    public StopsByLineDTO(List<IDFMStopGtfs> stops) {
        this.stops = stops;
    }

    public List<IDFMStopGtfs> getStops() {
        return stops;
    }

    public void setStops(List<IDFMStopGtfs> stops) {
        this.stops = stops;
    }
}
