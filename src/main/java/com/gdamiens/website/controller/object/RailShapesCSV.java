package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class RailShapesCSV {

    @CsvBindByPosition(position = 3)
    private String lineId;

    @CsvBindByPosition(position = 1)
    private String shape;

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }
}

