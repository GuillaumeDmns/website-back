package com.gdamiens.website.model;

import com.gdamiens.website.controller.object.LineCSV;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity(name = "IDFMLine")
@Table(schema = "public", name = "idfm_line")
public class IDFMLine implements Serializable {

    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "transport_mode")
    private String transportMode;

    public IDFMLine() {
    }

    public IDFMLine(String id) {
        this.id = id;
    }

    public IDFMLine(LineCSV lineCSV) {
        this.id = lineCSV.getStopId();
        this.name = lineCSV.getName();
        this.transportMode = lineCSV.getTransportMode();
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

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    @Override
    public String toString() {
        return "IDFMLine{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", transportMode='" + transportMode + '\'' +
            '}';
    }
}
