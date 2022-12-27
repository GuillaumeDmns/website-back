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

    @Column(name = "operator_id")
    private Integer operatorId;

    @Column(name = "line_id_color")
    private String lineIdColor;

    @Column(name = "line_id_background_color")
    private String lineIdBackgroundColor;

    public IDFMLine() {
    }

    public IDFMLine(String id) {
        this.id = id;
    }

    public IDFMLine(LineCSV lineCSV) {
        this.id = lineCSV.getLineId();
        this.name = lineCSV.getName();
        this.transportMode = lineCSV.getTransportMode();
        this.operatorId = lineCSV.getOperatorId();
        this.lineIdColor = lineCSV.getLineIdColor();
        this.lineIdBackgroundColor = lineCSV.getLineIdBackgroundColor();
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

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getLineIdColor() {
        return lineIdColor;
    }

    public void setLineIdColor(String lineIdColor) {
        this.lineIdColor = lineIdColor;
    }

    public String getLineIdBackgroundColor() {
        return lineIdBackgroundColor;
    }

    public void setLineIdBackgroundColor(String lineIdBackgroundColor) {
        this.lineIdBackgroundColor = lineIdBackgroundColor;
    }

    @Override
    public String toString() {
        return "IDFMLine{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", transportMode='" + transportMode + '\'' +
            ", operatorId=" + operatorId +
            ", lineIdColor='" + lineIdColor + '\'' +
            ", lineIdBackgroundColor='" + lineIdBackgroundColor + '\'' +
            '}';
    }
}
