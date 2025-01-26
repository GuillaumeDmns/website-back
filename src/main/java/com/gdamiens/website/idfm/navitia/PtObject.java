package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "quality",
    "stop_area",
    "stop_point",
    "poi",
    "line",
    "network",
    "route",
    "commercial_mode",
    "trip",
    "embedded_type"
})
@Generated("jsonschema2pojo")
public class PtObject {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("quality")
    private Integer quality;

    private StopArea stopArea;

    private StopPoint stopPoint;

    @JsonProperty("poi")
    private Poi poi;

    @JsonProperty("line")
    private Line line;

    @JsonProperty("network")
    private Network network;

    @JsonProperty("route")
    private Route route;

    private CommercialMode commercialMode;

    @JsonProperty("trip")
    private Trip trip;

    private EmbeddedTypeEnum embeddedType;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("quality")
    public Integer getQuality() {
        return quality;
    }

    @JsonProperty("quality")
    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    @JsonProperty("stopArea")
    public StopArea getStopArea() {
        return stopArea;
    }

    @JsonProperty("stop_area")
    public void setStopArea(StopArea stopArea) {
        this.stopArea = stopArea;
    }

    @JsonProperty("stopPoint")
    public StopPoint getStopPoint() {
        return stopPoint;
    }

    @JsonProperty("stop_point")
    public void setStopPoint(StopPoint stopPoint) {
        this.stopPoint = stopPoint;
    }

    @JsonProperty("poi")
    public Poi getPoi() {
        return poi;
    }

    @JsonProperty("poi")
    public void setPoi(Poi poi) {
        this.poi = poi;
    }

    @JsonProperty("line")
    public Line getLine() {
        return line;
    }

    @JsonProperty("line")
    public void setLine(Line line) {
        this.line = line;
    }

    @JsonProperty("network")
    public Network getNetwork() {
        return network;
    }

    @JsonProperty("network")
    public void setNetwork(Network network) {
        this.network = network;
    }

    @JsonProperty("route")
    public Route getRoute() {
        return route;
    }

    @JsonProperty("route")
    public void setRoute(Route route) {
        this.route = route;
    }

    @JsonProperty("commercialMode")
    public CommercialMode getCommercialMode() {
        return commercialMode;
    }

    @JsonProperty("commercial_mode")
    public void setCommercialMode(CommercialMode commercialMode) {
        this.commercialMode = commercialMode;
    }

    @JsonProperty("trip")
    public Trip getTrip() {
        return trip;
    }

    @JsonProperty("trip")
    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    @JsonProperty("embeddedType")
    public EmbeddedTypeEnum getEmbeddedType() {
        return embeddedType;
    }

    @JsonProperty("embedded_type")
    public void setEmbeddedType(EmbeddedTypeEnum embeddedType) {
        this.embeddedType = embeddedType;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
