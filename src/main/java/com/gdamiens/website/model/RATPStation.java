package com.gdamiens.website.model;

import com.gdamiens.website.ratp.wsdl.Station;

import java.io.Serializable;

public class RATPStation implements Serializable {

    private String id;

    private String name;

    private String lineId;

    private String directionName;

    public RATPStation() {}

    public RATPStation(String id, String name, String lineId, String directionId) {
        this.id = id;
        this.name = name;
        this.lineId = lineId;
        this.directionName = directionId;
    }

    public RATPStation(Station station) {
        this.id = station.getId();
        this.name = station.getName();
        if (station.getLine() != null) {
            this.lineId = station.getLine().getId();
        }
        if (station.getDirection() != null) {
            this.directionName = station.getDirection().getName();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getDirectionName() {
        return directionName;
    }

    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }
}
