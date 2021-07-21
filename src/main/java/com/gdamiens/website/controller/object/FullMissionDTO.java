package com.gdamiens.website.controller.object;

import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public class FullMissionDTO extends RATPResponse {

    private StationsDTO stationsDTO;

    private List<Triple<String, String, Long>> links;

    public FullMissionDTO(StationsDTO stationsDTO, List<Triple<String, String, Long>> links) {
        this.stationsDTO = stationsDTO;
        this.links = links;
    }

    public StationsDTO getStationsDTO() {
        return stationsDTO;
    }

    public void setStationsDTO(StationsDTO stationsDTO) {
        this.stationsDTO = stationsDTO;
    }

    public List<Triple<String, String, Long>> getLinks() {
        return links;
    }

    public void setLinks(List<Triple<String, String, Long>> links) {
        this.links = links;
    }
}
