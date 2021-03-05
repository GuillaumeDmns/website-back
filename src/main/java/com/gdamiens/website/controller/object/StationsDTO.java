package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.Station;
import com.gdamiens.website.ratp.wsdl.WrStations;

import java.util.List;

public class StationsDTO extends RATPResponse {

    private List<Station> stations;

    public StationsDTO(WrStations wrStations) {
        super();
        this.stations = wrStations.getStations();
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }
}

