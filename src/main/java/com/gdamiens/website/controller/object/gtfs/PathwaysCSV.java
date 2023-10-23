package com.gdamiens.website.controller.object.gtfs;

import com.opencsv.bean.CsvBindByPosition;

public class PathwaysCSV {

    @CsvBindByPosition(position = 0)
    private String pathways_id;

    @CsvBindByPosition(position = 1)
    private String from_stop_id;

    @CsvBindByPosition(position = 2)
    private String to_stop_id;

    @CsvBindByPosition(position = 3)
    private Integer pathway_mode;

    @CsvBindByPosition(position = 4)
    private Integer is_bidirectional;

    @CsvBindByPosition(position = 5)
    private Double length;

    @CsvBindByPosition(position = 6)
    private Integer transversal_time;

    @CsvBindByPosition(position = 7)
    private Integer stair_count;

    @CsvBindByPosition(position = 8)
    private Integer max_slope;

    @CsvBindByPosition(position = 9)
    private Double min_windth;

    @CsvBindByPosition(position = 10)
    private String signposted_as;

    @CsvBindByPosition(position = 11)
    private String reversed_signposted_as;

    public String getPathways_id() {
        return pathways_id;
    }

    public void setPathways_id(String pathways_id) {
        this.pathways_id = pathways_id;
    }

    public String getFrom_stop_id() {
        return from_stop_id;
    }

    public void setFrom_stop_id(String from_stop_id) {
        this.from_stop_id = from_stop_id;
    }

    public String getTo_stop_id() {
        return to_stop_id;
    }

    public void setTo_stop_id(String to_stop_id) {
        this.to_stop_id = to_stop_id;
    }

    public Integer getPathway_mode() {
        return pathway_mode;
    }

    public void setPathway_mode(Integer pathway_mode) {
        this.pathway_mode = pathway_mode;
    }

    public Integer getIs_bidirectional() {
        return is_bidirectional;
    }

    public void setIs_bidirectional(Integer is_bidirectional) {
        this.is_bidirectional = is_bidirectional;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Integer getTransversal_time() {
        return transversal_time;
    }

    public void setTransversal_time(Integer transversal_time) {
        this.transversal_time = transversal_time;
    }

    public Integer getStair_count() {
        return stair_count;
    }

    public void setStair_count(Integer stair_count) {
        this.stair_count = stair_count;
    }

    public Integer getMax_slope() {
        return max_slope;
    }

    public void setMax_slope(Integer max_slope) {
        this.max_slope = max_slope;
    }

    public Double getMin_windth() {
        return min_windth;
    }

    public void setMin_windth(Double min_windth) {
        this.min_windth = min_windth;
    }

    public String getSignposted_as() {
        return signposted_as;
    }

    public void setSignposted_as(String signposted_as) {
        this.signposted_as = signposted_as;
    }

    public String getReversed_signposted_as() {
        return reversed_signposted_as;
    }

    public void setReversed_signposted_as(String reversed_signposted_as) {
        this.reversed_signposted_as = reversed_signposted_as;
    }
}
