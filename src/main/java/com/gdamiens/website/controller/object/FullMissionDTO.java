package com.gdamiens.website.controller.object;

import org.springframework.data.util.Pair;

import java.util.List;

public class FullMissionDTO extends RATPResponse {

    private StationsDTO stationsDTO;

    private List<Pair<String, String>> links;

    public FullMissionDTO(StationsDTO stationsDTO, List<Pair<String, String>> links) {
        this.stationsDTO = stationsDTO;
        this.links = links;
    }

    public StationsDTO getStationsDTO() {
        return stationsDTO;
    }

    public void setStationsDTO(StationsDTO stationsDTO) {
        this.stationsDTO = stationsDTO;
    }

    public List<Pair<String, String>> getLinks() {
        return links;
    }

    public void setLinks(List<Pair<String, String>> links) {
        this.links = links;
    }
}
