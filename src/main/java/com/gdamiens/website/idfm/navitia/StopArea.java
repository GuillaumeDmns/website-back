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
    "comments",
    "comment",
    "codes",
    "timezone",
    "label",
    "coord",
    "links",
    "commercial_modes",
    "physical_modes",
    "administrative_regions",
    "stop_points",
    "lines"
})
@Generated("jsonschema2pojo")
public class StopArea {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("comments")
    private List<Comment> comments = null;

    @JsonProperty("comment")
    private Comment comment;

    @JsonProperty("codes")
    private List<Code> codes = null;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("label")
    private String label;

    @JsonProperty("coord")
    private Coord coord;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    @JsonProperty("commercial_modes")
    private List<CommercialMode> commercialModes = null;

    @JsonProperty("physical_modes")
    private List<PhysicalMode> physicalModes = null;

    @JsonProperty("administrative_regions")
    private List<Admin> administrativeRegions = null;

    @JsonProperty("stop_points")
    private List<StopPoint> stopPoints = null;

    @JsonProperty("lines")
    private List<Line> lines = null;

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

    @JsonProperty("comments")
    public List<Comment> getComments() {
        return comments;
    }

    @JsonProperty("comments")
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @JsonProperty("comment")
    public Comment getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(Comment comment) {
        this.comment = comment;
    }

    @JsonProperty("codes")
    public List<Code> getCodes() {
        return codes;
    }

    @JsonProperty("codes")
    public void setCodes(List<Code> codes) {
        this.codes = codes;
    }

    @JsonProperty("timezone")
    public String getTimezone() {
        return timezone;
    }

    @JsonProperty("timezone")
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("coord")
    public Coord getCoord() {
        return coord;
    }

    @JsonProperty("coord")
    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
    }

    @JsonProperty("commercial_modes")
    public List<CommercialMode> getCommercialModes() {
        return commercialModes;
    }

    @JsonProperty("commercial_modes")
    public void setCommercialModes(List<CommercialMode> commercialModes) {
        this.commercialModes = commercialModes;
    }

    @JsonProperty("physical_modes")
    public List<PhysicalMode> getPhysicalModes() {
        return physicalModes;
    }

    @JsonProperty("physical_modes")
    public void setPhysicalModes(List<PhysicalMode> physicalModes) {
        this.physicalModes = physicalModes;
    }

    @JsonProperty("administrative_regions")
    public List<Admin> getAdministrativeRegions() {
        return administrativeRegions;
    }

    @JsonProperty("administrative_regions")
    public void setAdministrativeRegions(List<Admin> administrativeRegions) {
        this.administrativeRegions = administrativeRegions;
    }

    @JsonProperty("stop_points")
    public List<StopPoint> getStopPoints() {
        return stopPoints;
    }

    @JsonProperty("stop_points")
    public void setStopPoints(List<StopPoint> stopPoints) {
        this.stopPoints = stopPoints;
    }

    @JsonProperty("lines")
    public List<Line> getLines() {
        return lines;
    }

    @JsonProperty("lines")
    public void setLines(List<Line> lines) {
        this.lines = lines;
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
