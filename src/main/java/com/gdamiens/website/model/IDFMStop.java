package com.gdamiens.website.model;

import com.gdamiens.website.controller.object.StopCSV;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity(name = "IDFMStop")
@Table(schema = "public", name = "idfm_stop")
public class IDFMStop implements Serializable {

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

    @Column(name = "stop_area_id")
    private Integer stopAreaId;

    public IDFMStop() {
    }

    public IDFMStop(Integer stopAreaId, Integer stopId) {
        this.stopAreaId = stopAreaId;
        this.id = stopId;
    }

    public IDFMStop(StopCSV stopCSV) {
        this.id = Integer.parseInt(stopCSV.getStopId());
        this.name = stopCSV.getStopName();
        this.latitude = stopCSV.getLatitude();
        this.longitude = stopCSV.getLongitude();
        this.type = stopCSV.getStopType();
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

    public Integer getStopAreaId() {
        return stopAreaId;
    }

    public void setStopAreaId(Integer stopAreaId) {
        this.stopAreaId = stopAreaId;
    }

    @Override
    public String toString() {
        return "IDFMStop{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", type='" + type + '\'' +
            ", stopAreaId=" + stopAreaId +
            '}';
    }
}
