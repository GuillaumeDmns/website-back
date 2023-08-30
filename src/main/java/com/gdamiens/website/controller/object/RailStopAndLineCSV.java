package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class RailStopAndLineCSV {

    @CsvBindByPosition(position = 8)
    private Integer stopAreaId;

    @CsvBindByPosition(position = 11)
    private String lineRef;

    public Integer getStopAreaId() {
        return stopAreaId;
    }

    public void setStopAreaId(Integer stopAreaId) {
        this.stopAreaId = stopAreaId;
    }

    public String getLineRef() {
        return lineRef;
    }

    public void setLineRef(String lineRef) {
        this.lineRef = lineRef;
    }
}
