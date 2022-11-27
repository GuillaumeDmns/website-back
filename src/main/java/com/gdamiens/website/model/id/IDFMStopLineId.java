package com.gdamiens.website.model.id;

import java.io.Serializable;
import java.util.Objects;

public class IDFMStopLineId implements Serializable {

    private String lineId;

    private Integer stopId;

    public IDFMStopLineId() {
    }

    public IDFMStopLineId(String lineId, Integer stopId) {
        this.lineId = lineId;
        this.stopId = stopId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IDFMStopLineId that = (IDFMStopLineId) o;
        return Objects.equals(lineId, that.lineId) && Objects.equals(stopId, that.stopId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineId, stopId);
    }
}
