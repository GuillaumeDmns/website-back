package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.Station;
import com.gdamiens.website.ratp.wsdl.WrStations;

import java.util.List;

public class StationsDTO extends RATPResponse {

    private List<Station> lines;

    public StationsDTO(WrStations wrStations) {
        super();
        this.lines = wrStations.getStations();
    }

    public List<Station> getLines() {
        return lines;
    }

    public void setLines(List<Station> lines) {
        this.lines = lines;
    }
}

