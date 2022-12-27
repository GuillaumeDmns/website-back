package com.gdamiens.website.model;

import com.gdamiens.website.controller.object.StationCSV;

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

    public IDFMStop(Integer id) {
        this.id = id;
    }

    public IDFMStop(StationCSV stationCSV) {
        this.id = Integer.parseInt(stationCSV.getStopId());
        this.name = stationCSV.getStopName();
        if (stationCSV.getGps() != null && stationCSV.getGps().split(",").length == 2) {
            String[] splittedGps = stationCSV.getGps().split(",");
            this.latitude = Double.parseDouble(splittedGps[0]);
            this.longitude = Double.parseDouble(splittedGps[1]);
        }
        this.type = stationCSV.getStopType();
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
