package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.WrStations;

import java.util.List;
import java.util.stream.Collectors;

public class StationsDTO extends RATPResponse {

    private List<StationCustom> stations;

    public StationsDTO(WrStations wrStations) {
        super();
        this.stations = wrStations.getStations().stream().map(StationCustom::new).collect(Collectors.toList());
    }

    public List<StationCustom> getStations() {
        return stations;
    }

    public void setStations(List<StationCustom> stations) {
        this.stations = stations;
    }
}

