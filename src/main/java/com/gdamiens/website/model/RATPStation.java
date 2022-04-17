package com.gdamiens.website.model;

import com.gdamiens.website.ratp.wsdl.Station;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Entity(name = "RATPStation")
@Table(schema = "public", name = "ratp_station")
@IdClass(RATPStationId.class)
public class RATPStation implements Serializable {

    @Id
    private String id;

    @Id
    @Column(name = "line_id")
    private String lineId;

    @Column(name = "name")
    private String name;

    @Column(name = "coordinate")
    private String coordinate;

    private String directionName;

    @Column(name = "active")
    private boolean active;

    public RATPStation() {}

    public RATPStation(String id, String name, String coordinate, String lineId, String directionId, boolean active) {
        this.id = id;
        this.name = name;
        this.coordinate = coordinate;
        this.lineId = lineId;
        this.directionName = directionId;
        this.active = active;
    }

    public RATPStation(Station station) {
        this.id = station.getId();
        this.name = station.getName();
        this.coordinate = "todo";
        if (station.getLine() != null) {
            this.lineId = station.getLine().getId();
        }
        if (station.getDirection() != null) {
            this.directionName = station.getDirection().getName();
        }
        this.active = false;
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

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
