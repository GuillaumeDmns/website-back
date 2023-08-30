package com.gdamiens.website.model;

import com.gdamiens.website.controller.object.StopOperatorCSV;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity(name = "IDFMStopOperator")
@Table(schema = "public", name = "idfm_stop_operator")
public class IDFMStopOperator implements Serializable {

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

    @Column(name = "stop_id")
    private Integer stopId;

    public IDFMStopOperator() {
    }

    public IDFMStopOperator(Integer stopId, Integer stopOperatorId) {
        this.stopId = stopId;
        this.id = stopOperatorId;
    }

    public IDFMStopOperator(StopOperatorCSV stopOperator) {
        this.id = Integer.parseInt(stopOperator.getStopOperatorId());
        this.name = stopOperator.getName();
        this.latitude = stopOperator.getLatitude();
        this.longitude = stopOperator.getLongitude();
        this.type = stopOperator.getType();
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

    public Integer getStopId() {
        return stopId;
    }

    public void setStopId(Integer stopId) {
        this.stopId = stopId;
    }
}
