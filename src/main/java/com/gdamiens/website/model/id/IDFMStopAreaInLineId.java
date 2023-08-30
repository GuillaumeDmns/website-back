package com.gdamiens.website.model.id;

import java.io.Serializable;
import java.util.Objects;

public class IDFMStopAreaInLineId implements Serializable {

    private String lineId;

    private Integer stopAreaId;

    public IDFMStopAreaInLineId() {
    }

    public IDFMStopAreaInLineId(String lineId, Integer stopAreaId) {
        this.lineId = lineId;
        this.stopAreaId = stopAreaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IDFMStopAreaInLineId that = (IDFMStopAreaInLineId) o;
        return Objects.equals(lineId, that.lineId) && Objects.equals(stopAreaId, that.stopAreaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineId, stopAreaId);
    }
}
