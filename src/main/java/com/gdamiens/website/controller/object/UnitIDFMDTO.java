package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMStopGtfs;

import java.util.List;
import java.util.stream.Collectors;

public class UnitIDFMDTO {

    private IDFMStopGtfs stopGtfs;

    private List<CallUnit> nextPassages;

    private List<String> nextPassageDestinations;


    public UnitIDFMDTO(IDFMStopGtfs stopGtfs, List<CallUnit> nextPassages) {
        this.stopGtfs = stopGtfs;
        this.nextPassages = nextPassages;
        this.nextPassageDestinations = nextPassages.stream().map(CallUnit::getDestinationDisplay).distinct().collect(Collectors.toList());
    }

    public UnitIDFMDTO() {}

    public IDFMStopGtfs getStopGtfs() {
        return stopGtfs;
    }

    public void setStopGtfs(IDFMStopGtfs stopGtfs) {
        this.stopGtfs = stopGtfs;
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
