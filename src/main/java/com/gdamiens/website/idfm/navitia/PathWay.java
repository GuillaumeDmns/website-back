package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "access_point",
    "is_entrance",
    "is_exit",
    "administrative_region",
    "traversal_time",
    "pathway_mode",
    "stair_count",
    "max_slope",
    "min_width",
    "signposted_as",
    "reversed_signposted_as"
})
@Generated("jsonschema2pojo")
public class PathWay {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("access_point")
    private AccessPoint accessPoint;

    @JsonProperty("is_entrance")
    private Boolean isEntrance;

    @JsonProperty("is_exit")
    private Boolean isExit;

    @JsonProperty("length")
    private Integer length;

    @JsonProperty("traversal_time")
    private Integer traversalTime;

    @JsonProperty("pathway_mode")
    private Integer pathwayMode;

    @JsonProperty("stair_count")
    private Integer stairCount;

    @JsonProperty("max_slope")
    private Integer maxSlope;

    @JsonProperty("min_width")
    private Integer minWidth;

    @JsonProperty("signposted_as")
    private String signpostedAs;

    @JsonProperty("reversed_signposted_as")
    private String reversedSignpostedAs;

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

    @JsonProperty("access_point")
    public AccessPoint getAccessPoint() {
        return accessPoint;
    }

    @JsonProperty("access_point")
    public void setAccessPoint(AccessPoint accessPoint) {
        this.accessPoint = accessPoint;
    }

    @JsonProperty("is_entrance")
    public Boolean getEntrance() {
        return isEntrance;
    }

    @JsonProperty("is_entrance")
    public void setEntrance(Boolean entrance) {
        isEntrance = entrance;
    }

    @JsonProperty("is_exit")
    public Boolean getExit() {
        return isExit;
    }

    @JsonProperty("is_exit")
    public void setExit(Boolean exit) {
        isExit = exit;
    }

    @JsonProperty("length")
    public Integer getLength() {
        return length;
    }

    @JsonProperty("length")
    public void setLength(Integer length) {
        this.length = length;
    }

    @JsonProperty("traversal_time")
    public Integer getTraversalTime() {
        return traversalTime;
    }

    @JsonProperty("traversal_time")
    public void setTraversalTime(Integer traversalTime) {
        this.traversalTime = traversalTime;
    }

    @JsonProperty("pathway_mode")
    public Integer getPathwayMode() {
        return pathwayMode;
    }

    @JsonProperty("pathway_mode")
    public void setPathwayMode(Integer pathwayMode) {
        this.pathwayMode = pathwayMode;
    }

    @JsonProperty("stair_count")
    public Integer getStairCount() {
        return stairCount;
    }

    @JsonProperty("stair_count")
    public void setStairCount(Integer stairCount) {
        this.stairCount = stairCount;
    }

    @JsonProperty("max_slope")
    public Integer getMaxSlope() {
        return maxSlope;
    }

    @JsonProperty("max_slope")
    public void setMaxSlope(Integer maxSlope) {
        this.maxSlope = maxSlope;
    }

    @JsonProperty("min_width")
    public Integer getMinWidth() {
        return minWidth;
    }

    @JsonProperty("min_width")
    public void setMinWidth(Integer minWidth) {
        this.minWidth = minWidth;
    }

    @JsonProperty("signposted_as")
    public String getSignpostedAs() {
        return signpostedAs;
    }

    @JsonProperty("signposted_as")
    public void setSignpostedAs(String signpostedAs) {
        this.signpostedAs = signpostedAs;
    }

    @JsonProperty("reversed_signposted_as")
    public String getReversedSignpostedAs() {
        return reversedSignpostedAs;
    }

    @JsonProperty("reversed_signposted_as")
    public void setReversedSignpostedAs(String reversedSignpostedAs) {
        this.reversedSignpostedAs = reversedSignpostedAs;
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
