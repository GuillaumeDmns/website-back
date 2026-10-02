package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMRoute;
import com.gdamiens.website.model.TransportMode;

public class LineDTO {

    private String id;

    private String name;

    private TransportMode transportMode;

    private String lineIdColor;

    private String lineIdBackgroundColor;

    public LineDTO(IDFMRoute route, TransportMode transportMode) {
        this.id = IDFMRoute.toLineId(route.getId());
        this.name = route.getShort_name();
        this.transportMode = transportMode;
        this.lineIdColor = route.getText_color();
        this.lineIdBackgroundColor = route.getColor();
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
}
