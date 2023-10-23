package com.gdamiens.website.controller.object.gtfs;

import com.opencsv.bean.CsvBindByPosition;

public class StopsCSV {
    @CsvBindByPosition(position = 0)
    private String stop_id;

    @CsvBindByPosition(position = 1)
    private String stop_code;

    @CsvBindByPosition(position = 2)
    private String stop_name;

    @CsvBindByPosition(position = 3)
    private String stop_desc;

    @CsvBindByPosition(position = 4)
    private Double stop_lat;

    @CsvBindByPosition(position = 5)
    private Double stop_lon;

    @CsvBindByPosition(position = 6)
    private Integer zone_id;

    @CsvBindByPosition(position = 7)
    private String stop_url;

    @CsvBindByPosition(position = 8)
    private String location_type;

    @CsvBindByPosition(position = 9)
    private String parent_station;

    @CsvBindByPosition(position = 10)
    private Integer wheelchair_boarding;

    @CsvBindByPosition(position = 11)
    private String stop_timezone;

    @CsvBindByPosition(position = 12)
    private String level_id;

    @CsvBindByPosition(position = 13)
    private String platform_code;

    public String getStop_id() {
        return stop_id;
    }

    public void setStop_id(String stop_id) {
        this.stop_id = stop_id;
    }

    public String getStop_code() {
        return stop_code;
    }

    public void setStop_code(String stop_code) {
        this.stop_code = stop_code;
    }

    public String getStop_name() {
        return stop_name;
    }

    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    public String getStop_desc() {
        return stop_desc;
    }

    public void setStop_desc(String stop_desc) {
        this.stop_desc = stop_desc;
    }

    public Double getStop_lat() {
        return stop_lat;
    }

    public void setStop_lat(Double stop_lat) {
        this.stop_lat = stop_lat;
    }

    public Double getStop_lon() {
        return stop_lon;
    }

    public void setStop_lon(Double stop_lon) {
        this.stop_lon = stop_lon;
    }

    public Integer getZone_id() {
        return zone_id;
    }

    public void setZone_id(Integer zone_id) {
        this.zone_id = zone_id;
    }

    public String getStop_url() {
        return stop_url;
    }

    public void setStop_url(String stop_url) {
        this.stop_url = stop_url;
    }

    public String getLocation_type() {
        return location_type;
    }

    public void setLocation_type(String location_type) {
        this.location_type = location_type;
    }

    public String getParent_station() {
        return parent_station;
    }

    public void setParent_station(String parent_station) {
        this.parent_station = parent_station;
    }

    public Integer getWheelchair_boarding() {
        return wheelchair_boarding;
    }

    public void setWheelchair_boarding(Integer wheelchair_boarding) {
        this.wheelchair_boarding = wheelchair_boarding;
    }

    public String getStop_timezone() {
        return stop_timezone;
    }

    public void setStop_timezone(String stop_timezone) {
        this.stop_timezone = stop_timezone;
    }

    public String getLevel_id() {
        return level_id;
    }

    public void setLevel_id(String level_id) {
        this.level_id = level_id;
    }

    public String getPlatform_code() {
        return platform_code;
    }

    public void setPlatform_code(String platform_code) {
        this.platform_code = platform_code;
    }
}
