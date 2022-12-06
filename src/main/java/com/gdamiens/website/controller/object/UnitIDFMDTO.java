package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMStop;

import java.util.List;

public class UnitIDFMDTO {

    private IDFMStop stop;

    private List<CallUnit> nextPassages;

    public UnitIDFMDTO(IDFMStop idfmStop, List<CallUnit> nextPassages) {
        this.stop = idfmStop;
        this.nextPassages = nextPassages;
    }

    public UnitIDFMDTO() {}

    public IDFMStop getStop() {
        return stop;
    }

    public void setStop(IDFMStop stop) {
        this.stop = stop;
    }

    public List<CallUnit> getNextPassages() {
        return nextPassages;
    }

    public void setNextPassages(List<CallUnit> nextPassages) {
        this.nextPassages = nextPassages;
    }
}
