package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class StopAndLineCSV {

    @CsvBindByPosition(position = 4)
    private String stopId;

    @CsvBindByPosition(position = 0)
    private String lineRef;

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getLineRef() {
        return lineRef;
    }

    public void setLineRef(String lineRef) {
        this.lineRef = lineRef;
    }
}
