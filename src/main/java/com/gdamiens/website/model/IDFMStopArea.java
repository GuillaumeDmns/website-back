package com.gdamiens.website.model;

import com.gdamiens.website.controller.object.StopAreaCSV;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity(name = "IDFMStopArea")
@Table(schema = "public", name = "idfm_stop_area")
public class IDFMStopArea implements Serializable {

    @Id
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "type")
    private String type;

    public IDFMStopArea() {
    }

    public IDFMStopArea(StopAreaCSV stopAreaCSV) {

        this.id = Integer.parseInt(stopAreaCSV.getStopAreaId());
        this.name = stopAreaCSV.getName();
        this.latitude = stopAreaCSV.getLatitude();
        this.longitude = stopAreaCSV.getLongitude();
        this.type = stopAreaCSV.getType();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
