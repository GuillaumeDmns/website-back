package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.TransportMode;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import java.util.Optional;

public class LineDTO {

    private String id;

    private String name;

    private TransportMode transportMode;

    private Integer operatorId;

    private String lineIdColor;

    private String lineIdBackgroundColor;

    private String shape;

    public LineDTO(IDFMLine idfmLine) {
        this.id = idfmLine.getId();
        this.name = idfmLine.getName();
        this.transportMode = idfmLine.getTransportMode();
        this.operatorId = idfmLine.getOperatorId();
        this.lineIdColor = idfmLine.getLineIdColor();
        this.lineIdBackgroundColor = idfmLine.getLineIdBackgroundColor();
        GeoJsonWriter writer = new GeoJsonWriter();
        this.shape = Optional.ofNullable(idfmLine.getShape()).map(writer::write).orElse(null) ;
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

    public TransportMode getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(TransportMode transportMode) {
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

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }
}
