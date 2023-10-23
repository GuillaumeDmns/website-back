package com.gdamiens.website.controller.object.gtfs;

import com.opencsv.bean.CsvBindByPosition;

public class RoutesCSV {

    @CsvBindByPosition(position = 0)
    private String route_id;

    @CsvBindByPosition(position = 1)
    private String agency_id;

    @CsvBindByPosition(position = 2)
    private String route_short_name;

    @CsvBindByPosition(position = 3)
    private String route_long_name;

    @CsvBindByPosition(position = 4)
    private Integer route_desc;

    @CsvBindByPosition(position = 5)
    private Integer route_type;

    @CsvBindByPosition(position = 6)
    private String route_url;

    @CsvBindByPosition(position = 7)
    private String route_color;

    @CsvBindByPosition(position = 8)
    private String route_text_color;

    @CsvBindByPosition(position = 9)
    private Integer route_sort_order;

    public String getRoute_id() {
        return route_id;
    }

    public void setRoute_id(String route_id) {
        this.route_id = route_id;
    }

    public String getAgency_id() {
        return agency_id;
    }

    public void setAgency_id(String agency_id) {
        this.agency_id = agency_id;
    }

    public String getRoute_short_name() {
        return route_short_name;
    }

    public void setRoute_short_name(String route_short_name) {
        this.route_short_name = route_short_name;
    }

    public String getRoute_long_name() {
        return route_long_name;
    }

    public void setRoute_long_name(String route_long_name) {
        this.route_long_name = route_long_name;
    }

    public Integer getRoute_desc() {
        return route_desc;
    }

    public void setRoute_desc(Integer route_desc) {
        this.route_desc = route_desc;
    }

    public Integer getRoute_type() {
        return route_type;
    }

    public void setRoute_type(Integer route_type) {
        this.route_type = route_type;
    }

    public String getRoute_url() {
        return route_url;
    }

    public void setRoute_url(String route_url) {
        this.route_url = route_url;
    }

    public String getRoute_color() {
        return route_color;
    }

    public void setRoute_color(String route_color) {
        this.route_color = route_color;
    }

    public String getRoute_text_color() {
        return route_text_color;
    }

    public void setRoute_text_color(String route_text_color) {
        this.route_text_color = route_text_color;
    }

    public Integer getRoute_sort_order() {
        return route_sort_order;
    }

    public void setRoute_sort_order(Integer route_sort_order) {
        this.route_sort_order = route_sort_order;
    }
}
