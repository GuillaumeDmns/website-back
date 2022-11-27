package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class CSV {

    @CsvBindByPosition(position = 0)
    private String monitoringRefZDE;

    @CsvBindByPosition(position = 1)
    private String lineRef;

    public String getMonitoringRefZDE() {
        return monitoringRefZDE;
    }

    public void setMonitoringRefZDE(String monitoringRefZDE) {
        this.monitoringRefZDE = monitoringRefZDE;
    }

    public String getLineRef() {
        return lineRef;
    }

    public void setLineRef(String lineRef) {
        this.lineRef = lineRef;
    }
}
