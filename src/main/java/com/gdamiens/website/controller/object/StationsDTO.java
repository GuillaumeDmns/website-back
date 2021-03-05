package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.Station;

import java.util.List;

public class StationsDTO extends RATPResponse {

    private List<Station> lines;

    public StationsDTO(List<Station> lines) {
        this.lines = lines;
    }

    public List<Station> getLines() {
        return lines;
    }

    public void setLines(List<Station> lines) {
        this.lines = lines;
    }
}

