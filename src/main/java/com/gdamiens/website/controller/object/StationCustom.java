package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.Direction;
import com.gdamiens.website.ratp.wsdl.GeoPoint;
import com.gdamiens.website.ratp.wsdl.Station;
import com.gdamiens.website.ratp.wsdl.StationArea;

import java.util.List;

public class StationCustom {

    private String id;

    private String name;

    private LineCustom line;

    private Direction direction;

    private GeoPoint geoPointA;

    private GeoPoint geoPointR;

    private StationArea stationArea;

    private List<String> nextA;

    private List<String> nextR;

    public StationCustom(Station station) {
        if (station != null) {
            this.id = station.getId();
            this.name = station.getName();
            this.line = station.getLine() == null ? null : new LineCustom(station.getLine());
            this.direction = station.getDirection();
            this.geoPointA = station.getGeoPointA();
            this.geoPointR = station.getGeoPointR();
            this.stationArea = station.getStationArea();
            this.nextA = station.getIdsNextA();
            this.nextR = station.getIdsNextR();
        }
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

    public LineCustom getLine() {
        return line;
    }

    public void setLine(LineCustom line) {
        this.line = line;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public GeoPoint getGeoPointA() {
        return geoPointA;
    }

    public void setGeoPointA(GeoPoint geoPointA) {
        this.geoPointA = geoPointA;
    }

    public GeoPoint getGeoPointR() {
        return geoPointR;
    }

    public void setGeoPointR(GeoPoint geoPointR) {
        this.geoPointR = geoPointR;
    }

    public StationArea getStationArea() {
        return stationArea;
    }

    public void setStationArea(StationArea stationArea) {
        this.stationArea = stationArea;
    }

    public List<String> getNextA() {
        return nextA;
    }

    public void setNextA(List<String> nextA) {
        this.nextA = nextA;
    }

    public List<String> getNextR() {
        return nextR;
    }

    public void setNextR(List<String> nextR) {
        this.nextR = nextR;
    }
}
