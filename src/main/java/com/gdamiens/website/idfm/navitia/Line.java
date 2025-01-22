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
    "code",
    "color",
    "text_color",
    "comments",
    "comment",
    "codes",
    "commercial_mode",
    "physical_modes",
    "routes",
    "network",
    "opening_time",
    "closing_time",
    "properties",
    "geojson",
    "links",
    "line_groups"
})
@Generated("jsonschema2pojo")
public class Line {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @JsonProperty("color")
    private String color;

    private String textColor = null;

    @JsonProperty("comments")
    private List<Comment> comments = null;

    @JsonProperty("comment")
    private Comment comment;

    @JsonProperty("codes")
    private List<Code> codes = null;

    private CommercialMode commercialMode = null;

    private List<PhysicalMode> physicalModes = null;

    @JsonProperty("routes")
    private List<Route> routes = null;

    @JsonProperty("network")
    private Network network;

    private String openingTime;

    private String closingTime;

    @JsonProperty("properties")
    private List<Property> properties = null;

    @JsonProperty("geojson")
    private MultiLineStringSchema geojson;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    private List<LineGroup> lineGroups = null;

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

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("color")
    public String getColor() {
        return color;
    }

    @JsonProperty("color")
    public void setColor(String color) {
        this.color = color;
    }

    @JsonProperty("textColor")
    public String getTextColor() {
        return textColor;
    }

    @JsonProperty("text_color")
    public void setTextColor(String textColor) {
        this.textColor = textColor;
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

    @JsonProperty("commercialMode")
    public CommercialMode getCommercialMode() {
        return commercialMode;
    }

    @JsonProperty("commercial_mode")
    public void setCommercialMode(CommercialMode commercialMode) {
        this.commercialMode = commercialMode;
    }

    @JsonProperty("physicalModes")
    public List<PhysicalMode> getPhysicalModes() {
        return physicalModes;
    }

    @JsonProperty("physical_modes")
    public void setPhysicalModes(List<PhysicalMode> physicalModes) {
        this.physicalModes = physicalModes;
    }

    @JsonProperty("routes")
    public List<Route> getRoutes() {
        return routes;
    }

    @JsonProperty("routes")
    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    @JsonProperty("network")
    public Network getNetwork() {
        return network;
    }

    @JsonProperty("network")
    public void setNetwork(Network network) {
        this.network = network;
    }

    @JsonProperty("openingTime")
    public String getOpeningTime() {
        return openingTime;
    }

    @JsonProperty("opening_time")
    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    @JsonProperty("closingTime")
    public String getClosingTime() {
        return closingTime;
    }

    @JsonProperty("closing_time")
    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    @JsonProperty("properties")
    public List<Property> getProperties() {
        return properties;
    }

    @JsonProperty("properties")
    public void setProperties(List<Property> properties) {
        this.properties = properties;
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

    @JsonProperty("lineGroups")
    public List<LineGroup> getLineGroups() {
        return lineGroups;
    }

    @JsonProperty("line_groups")
    public void setLineGroups(List<LineGroup> lineGroups) {
        this.lineGroups = lineGroups;
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
