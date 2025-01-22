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
    "stopArea",
    "stop_point",
    "administrative_region",
    "embedded_type",
    "address",
    "poi",
    "access_point",
    "distance"
})
@Generated("jsonschema2pojo")
public class Place {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("quality")
    private Integer quality;

    private StopArea stopArea;

    private StopPoint stopPoint;

    private Admin administrativeRegion;

    private EmbeddedTypeEnum embeddedType;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("poi")
    private Poi poi;

    private PathWay accessPoint;

    @JsonProperty("distance")
    private String distance;

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

    @JsonProperty("administrativeRegion")
    public Admin getAdministrativeRegion() {
        return administrativeRegion;
    }

    @JsonProperty("administrative_region")
    public void setAdministrativeRegion(Admin administrativeRegion) {
        this.administrativeRegion = administrativeRegion;
    }

    @JsonProperty("embeddedType")
    public EmbeddedTypeEnum getEmbeddedType() {
        return embeddedType;
    }

    @JsonProperty("embedded_type")
    public void setEmbeddedType(EmbeddedTypeEnum embeddedType) {
        this.embeddedType = embeddedType;
    }

    @JsonProperty("address")
    public Address getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(Address address) {
        this.address = address;
    }

    @JsonProperty("poi")
    public Poi getPoi() {
        return poi;
    }

    @JsonProperty("poi")
    public void setPoi(Poi poi) {
        this.poi = poi;
    }

    @JsonProperty("accessPoint")
    public PathWay getAccessPoint() {
        return accessPoint;
    }

    @JsonProperty("access_point")
    public void setAccessPoint(PathWay accessPoint) {
        this.accessPoint = accessPoint;
    }

    @JsonProperty("distance")
    public String getDistance() {
        return distance;
    }

    @JsonProperty("distance")
    public void setDistance(String distance) {
        this.distance = distance;
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
