package com.gdamiens.website.model;

import com.gdamiens.website.controller.object.gtfs.AgencyCSV;
import com.gdamiens.website.controller.object.gtfs.RoutesCSV;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity(name = "IDFMRoute")
@Table(schema = "public", name = "idfm_route")
public class IDFMRoute implements Serializable {

    @Id
    private String id;

    @Column(name = "agency_id")
    private String agency_id;

    @Column(name = "short_name")
    private String short_name;

    @Column(name = "long_name")
    private String long_name;

    @Column(name = "type")
    private Integer type;

    @Column(name = "color")
    private String color;

    @Column(name = "text_color")
    private String text_color;

    public IDFMRoute() {
    }

    public IDFMRoute(RoutesCSV routesCSV) {
        this.id = routesCSV.getRoute_id();
        this.agency_id = routesCSV.getAgency_id();
        this.short_name = routesCSV.getRoute_short_name();
        this.long_name = routesCSV.getRoute_long_name();
        this.type = routesCSV.getRoute_type();
        this.color = routesCSV.getRoute_color();
        this.text_color = routesCSV.getRoute_text_color();
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
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
