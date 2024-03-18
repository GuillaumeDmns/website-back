package com.gdamiens.website.model;

import com.gdamiens.website.controller.object.gtfs.TripsCSV;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity(name = "IDFMTrip")
@Table(schema = "public", name = "idfm_trip")
public class IDFMTrip implements Serializable {

    @Id
    private String id;

    @Column(name = "route_id")
    private String route_id;

    @Column(name = "service_id")
    private String service_id;

    @Column(name = "headsign")
    private String headsign;

    @Column(name = "short_name")
    private String short_name;

    @Column(name = "direction_id")
    private Integer direction_id;

    @Column(name = "wheelchair_accessible")
    private Integer wheelchair_accessible;

    @Column(name = "bikes_allowed")
    private Integer bikes_allowed;

    public IDFMTrip() {
    }

    public IDFMTrip(TripsCSV tripsCSV) {
        this.id = tripsCSV.getTrip_id();
        this.route_id = tripsCSV.getRoute_id();
        this.service_id = tripsCSV.getService_id();
        this.headsign = tripsCSV.getTrip_headsign();
        this.short_name = tripsCSV.getTrip_short_name();
        this.direction_id = tripsCSV.getDirection_id();
        this.wheelchair_accessible = tripsCSV.getWheelchair_accessible();
        this.bikes_allowed = tripsCSV.getBikes_allowed();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoute_id() {
        return route_id;
    }

    public void setRoute_id(String route_id) {
        this.route_id = route_id;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public Integer getDirection_id() {
        return direction_id;
    }

    public void setDirection_id(Integer direction_id) {
        this.direction_id = direction_id;
    }

    public Integer getWheelchair_accessible() {
        return wheelchair_accessible;
    }

    public void setWheelchair_accessible(Integer wheelchair_accessible) {
        this.wheelchair_accessible = wheelchair_accessible;
    }

    public Integer getBikes_allowed() {
        return bikes_allowed;
    }

    public void setBikes_allowed(Integer bikes_allowed) {
        this.bikes_allowed = bikes_allowed;
    }
}
