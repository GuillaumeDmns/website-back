package com.gdamiens.website.controller.object.gtfs;

import com.opencsv.bean.CsvBindByPosition;

public class TripsCSV {

    @CsvBindByPosition(position = 0)
    private String route_id;

    @CsvBindByPosition(position = 1)
    private String service_id;

    @CsvBindByPosition(position = 2)
    private String trip_id;

    @CsvBindByPosition(position = 3)
    private String trip_headsign;

    @CsvBindByPosition(position = 4)
    private String trip_short_name;

    @CsvBindByPosition(position = 5)
    private Boolean direction_id;

    @CsvBindByPosition(position = 6)
    private String block_id;

    @CsvBindByPosition(position = 7)
    private String shape_id;

    @CsvBindByPosition(position = 8)
    private Integer wheelchair_accessible;

    @CsvBindByPosition(position = 9)
    private Integer bikes_allowed;

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

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getTrip_headsign() {
        return trip_headsign;
    }

    public void setTrip_headsign(String trip_headsign) {
        this.trip_headsign = trip_headsign;
    }

    public String getTrip_short_name() {
        return trip_short_name;
    }

    public void setTrip_short_name(String trip_short_name) {
        this.trip_short_name = trip_short_name;
    }

    public Boolean getDirection_id() {
        return direction_id;
    }

    public void setDirection_id(Boolean direction_id) {
        this.direction_id = direction_id;
    }

    public String getBlock_id() {
        return block_id;
    }

    public void setBlock_id(String block_id) {
        this.block_id = block_id;
    }

    public String getShape_id() {
        return shape_id;
    }

    public void setShape_id(String shape_id) {
        this.shape_id = shape_id;
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
