package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMStopGtfs;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import java.util.List;
import java.util.Optional;

public class StopsByLineDTO {

    private List<IDFMStopGtfs> stops;

    private String shape;

    public StopsByLineDTO(List<IDFMStopGtfs> stops, Geometry shape) {
        this.stops = stops;
        GeoJsonWriter writer = new GeoJsonWriter();
        this.shape = Optional.ofNullable(shape).map(writer::write).orElse(null) ;
    }

    public List<IDFMStopGtfs> getStops() {
        return stops;
    }

    public void setStops(List<IDFMStopGtfs> stops) {
        this.stops = stops;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }
}
