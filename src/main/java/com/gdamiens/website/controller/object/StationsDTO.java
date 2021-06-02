package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.RATPStation;

import java.util.List;

public class StationsDTO extends RATPResponse {

    private List<RATPStation> stations;

    public StationsDTO(List<RATPStation> stations) {
        super();
        this.stations = stations;
    }

    public List<RATPStation> getStations() {
        return stations;
    }

    public void setStations(List<RATPStation> stations) {
        this.stations = stations;
    }
}

