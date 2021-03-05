package com.gdamiens.website.controller.object;


public class StationRequest {

    private String id;

    private String name;

    private String sens;

    private LineRequest line;

    private Integer limit;

    private Boolean isSortedAlpha;

    public StationRequest() {
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

    public String getSens() {
        return sens;
    }

    public void setSens(String sens) {
        this.sens = sens;
    }

    public LineRequest getLine() {
        return line;
    }

    public void setLine(LineRequest line) {
        this.line = line;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Boolean getIsSortedAlpha() {
        return isSortedAlpha;
    }

    public void setIsSortedAlpha(Boolean isSortedAlpha) {
        this.isSortedAlpha = isSortedAlpha;
    }
}
