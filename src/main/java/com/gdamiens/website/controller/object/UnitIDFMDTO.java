package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMStop;
import com.gdamiens.website.model.IDFMStopArea;

import java.util.List;
import java.util.stream.Collectors;

public class UnitIDFMDTO {

    private IDFMStop stop;

    private IDFMStopArea stopArea;

    private List<CallUnit> nextPassages;

    private List<String> nextPassageDestinations;

    public UnitIDFMDTO(IDFMStop idfmStop, List<CallUnit> nextPassages) {
        this.stop = idfmStop;
        this.nextPassages = nextPassages;
        this.nextPassageDestinations = nextPassages.stream().map(CallUnit::getDestinationDisplay).distinct().collect(Collectors.toList());
    }

    public UnitIDFMDTO(IDFMStopArea idfmStopArea, List<CallUnit> nextPassages) {
        this.stopArea = idfmStopArea;
        this.nextPassages = nextPassages;
        this.nextPassageDestinations = nextPassages.stream().map(CallUnit::getDestinationDisplay).distinct().collect(Collectors.toList());
    }

    public UnitIDFMDTO() {}

    public IDFMStop getStop() {
        return stop;
    }

    public void setStop(IDFMStop stop) {
        this.stop = stop;
    }

    public IDFMStopArea getStopArea() {
        return stopArea;
    }

    public void setStopArea(IDFMStopArea stopArea) {
        this.stopArea = stopArea;
    }

    public List<CallUnit> getNextPassages() {
        return nextPassages;
    }

    public void setNextPassages(List<CallUnit> nextPassages) {
        this.nextPassages = nextPassages;
    }

    public List<String> getNextPassageDestinations() {
        return nextPassageDestinations;
    }

    public void setNextPassageDestinations(List<String> nextPassageDestinations) {
        this.nextPassageDestinations = nextPassageDestinations;
    }
}
