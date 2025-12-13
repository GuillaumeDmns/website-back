package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class Via {
    @JsonProperty("id")
    String id;

    @JsonProperty("name")
    String name;

    AccessPoint accessPoint;

    Boolean isEntrance;

    Boolean isExit;

    @JsonProperty("length")
    int length;

    int traversalTime;

    int pathwayMode;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return this.id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return this.name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("accessPoint")
    public AccessPoint getAccessPoint() {
        return accessPoint;
    }

    @JsonProperty("access_point")
    public void setAccessPoint(AccessPoint accessPoint) {
        this.accessPoint = accessPoint;
    }

    @JsonProperty("isEntrance")
    public Boolean isEntrance() {
        return isEntrance;
    }

    @JsonProperty("is_entrance")
    public void setEntrance(Boolean entrance) {
        isEntrance = entrance;
    }

    @JsonProperty("isExit")
    public Boolean isExit() {
        return isExit;
    }

    @JsonProperty("is_exit")
    public void setExit(Boolean exit) {
        isExit = exit;
    }

    @JsonProperty("length")
    public int getLength() {
        return this.length;
    }

    @JsonProperty("length")
    public void setLength(int length) {
        this.length = length;
    }

    @JsonProperty("traversalTime")
    public int getTraversalTime() {
        return traversalTime;
    }

    @JsonProperty("traversal_time")
    public void setTraversalTime(int traversalTime) {
        this.traversalTime = traversalTime;
    }

    @JsonProperty("pathwayMode")
    public int getPathwayMode() {
        return pathwayMode;
    }

    @JsonProperty("pathway_mode")
    public void setPathwayMode(int pathwayMode) {
        this.pathwayMode = pathwayMode;
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
