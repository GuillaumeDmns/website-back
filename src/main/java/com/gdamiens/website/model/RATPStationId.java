package com.gdamiens.website.model;

import java.io.Serializable;
import java.util.Objects;

public class RATPStationId implements Serializable {
    private String id;

    private String lineId;

    public RATPStationId() {
    }

    public RATPStationId(String id, String lineId) {
        this.id = id;
        this.lineId = lineId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RATPStationId that = (RATPStationId) o;
        return Objects.equals(id, that.id) && Objects.equals(lineId, that.lineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lineId);
    }
}
