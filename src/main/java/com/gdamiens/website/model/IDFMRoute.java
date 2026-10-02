package com.gdamiens.website.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity(name = "IDFMRoute")
@Table(schema = "gtfs", name = "routes")
public class IDFMRoute implements Serializable {

    @Id
    @Column(name = "route_id")
    private String id;

    @Column(name = "agency_id")
    private String agency_id;

    @Column(name = "route_short_name")
    private String short_name;

    @Column(name = "route_long_name")
    private String long_name;

    @Column(name = "route_type")
    private Short type;

    @Column(name = "route_color")
    private String color;

    @Column(name = "route_text_color")
    private String text_color;

    public IDFMRoute() {
    }

    // The API exposes IDFM line ids (C01371) while GTFS route ids are prefixed (IDFM:C01371)
    public static String toRouteId(String lineId) {
        return "IDFM:" + lineId;
    }

    public static String toLineId(String routeId) {
        return routeId.substring(routeId.indexOf(':') + 1);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAgency_id() {
        return agency_id;
    }

    public void setAgency_id(String agency_id) {
        this.agency_id = agency_id;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getLong_name() {
        return long_name;
    }

    public void setLong_name(String long_name) {
        this.long_name = long_name;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getText_color() {
        return text_color;
    }

    public void setText_color(String text_color) {
        this.text_color = text_color;
    }
}
