package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "is_frequence",
    "direction_type",
    "physical_modes",
    "comments",
    "codes",
    "direction",
    "geojson",
    "links",
    "line",
    "stop_points"
})
@Generated("jsonschema2pojo")
public class Route {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    private IsFrequenceEnum isFrequence;

    private String directionType;

    private List<PhysicalMode> physicalModes = null;

    @JsonProperty("comments")
    private List<Comment> comments = null;

    @JsonProperty("codes")
    private List<Code> codes = null;

    @JsonProperty("direction")
    private Place direction;

    @JsonProperty("geojson")
    private MultiLineStringSchema geojson;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    @JsonProperty("line")
    private Line line;

    private List<StopPoint> stopPoints = null;

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

    @JsonProperty("isFrequence")
    public IsFrequenceEnum getIsFrequence() {
        return isFrequence;
    }

    @JsonProperty("is_frequence")
    public void setIsFrequence(IsFrequenceEnum isFrequence) {
        this.isFrequence = isFrequence;
    }

    @JsonProperty("directionType")
    public String getDirectionType() {
        return directionType;
    }

    @JsonProperty("direction_type")
    public void setDirectionType(String directionType) {
        this.directionType = directionType;
    }

    @JsonProperty("physicalModes")
    public List<PhysicalMode> getPhysicalModes() {
        return physicalModes;
    }

    @JsonProperty("physical_modes")
    public void setPhysicalModes(List<PhysicalMode> physicalModes) {
        this.physicalModes = physicalModes;
    }

    @JsonProperty("comments")
    public List<Comment> getComments() {
        return comments;
    }

    @JsonProperty("comments")
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @JsonProperty("codes")
    public List<Code> getCodes() {
        return codes;
    }

    @JsonProperty("codes")
    public void setCodes(List<Code> codes) {
        this.codes = codes;
    }

    @JsonProperty("direction")
    public Place getDirection() {
        return direction;
    }

    @JsonProperty("direction")
    public void setDirection(Place direction) {
        this.direction = direction;
    }

    @JsonProperty("geojson")
    public MultiLineStringSchema getGeojson() {
        return geojson;
    }

    @JsonProperty("geojson")
    public void setGeojson(MultiLineStringSchema geojson) {
        this.geojson = geojson;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
    }

    @JsonProperty("line")
    public Line getLine() {
        return line;
    }

    @JsonProperty("line")
    public void setLine(Line line) {
        this.line = line;
    }

    @JsonProperty("stopPoints")
    public List<StopPoint> getStopPoints() {
        return stopPoints;
    }

    @JsonProperty("stop_points")
    public void setStopPoints(List<StopPoint> stopPoints) {
        this.stopPoints = stopPoints;
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
