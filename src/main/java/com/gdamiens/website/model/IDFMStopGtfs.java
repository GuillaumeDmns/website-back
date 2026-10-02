package com.gdamiens.website.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity(name = "IDFMStopGtfs")
@Table(schema = "gtfs", name = "stops")
public class IDFMStopGtfs implements Serializable {

    @Id
    @Column(name = "stop_id")
    private String id;

    @Column(name = "stop_name")
    private String name;

    @Column(name = "stop_lat")
    private Double latitude;

    @Column(name = "stop_lon")
    private Double longitude;

    @Column(name = "parent_station")
    private String parentStation;

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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getParentStation() {
        return parentStation;
    }

    public void setParentStation(String parentStation) {
        this.parentStation = parentStation;
    }

    @Override
    public String toString() {
        return "IDFMStopGtfs{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", parentStation='" + parentStation + '\'' +
            '}';
    }
}
