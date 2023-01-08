package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMStopArea;

import java.util.List;

public class StopsByLineDTO {

    private List<IDFMStopArea> stops;

    public StopsByLineDTO(List<IDFMStopArea> stops) {
        this.stops = stops;
    }

    public List<IDFMStopArea> getStops() {
        return stops;
    }

    public void setStops(List<IDFMStopArea> stops) {
        this.stops = stops;
    }
}
