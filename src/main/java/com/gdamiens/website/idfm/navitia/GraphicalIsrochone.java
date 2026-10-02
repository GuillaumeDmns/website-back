package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class GraphicalIsrochone {

    private Integer maxDuration;

    @JsonProperty("from")
    private Place from;

    @JsonProperty("geojson")
    private Object geojson;

    private Integer minDuration;

    private String minDateTime;

    @JsonProperty("to")
    private Place to;

    private String requestedDateTime;

    private String maxDateTime;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("maxDuration")
    public Integer getMaxDuration() {
        return maxDuration;
    }

    @JsonProperty("max_duration")
    public void setMaxDuration(Integer maxDuration) {
        this.maxDuration = maxDuration;
    }

    @JsonProperty("from")
    public Place getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(Place from) {
        this.from = from;
    }

    @JsonProperty("geojson")
    public Object getGeojson() {
        return geojson;
    }

    @JsonProperty("geojson")
    public void setGeojson(Object geojson) {
        this.geojson = geojson;
    }

    @JsonProperty("minDuration")
    public Integer getMinDuration() {
        return minDuration;
    }

    @JsonProperty("min_duration")
    public void setMinDuration(Integer minDuration) {
        this.minDuration = minDuration;
    }

    @JsonProperty("minDateTime")
    public String getMinDateTime() {
        return minDateTime;
    }

    @JsonProperty("min_date_time")
    public void setMinDateTime(String minDateTime) {
        this.minDateTime = minDateTime;
    }

    @JsonProperty("to")
    public Place getTo() {
        return to;
    }

    @JsonProperty("to")
    public void setTo(Place to) {
        this.to = to;
    }

    @JsonProperty("requestedDateTime")
    public String getRequestedDateTime() {
        return requestedDateTime;
    }

    @JsonProperty("requested_date_time")
    public void setRequestedDateTime(String requestedDateTime) {
        this.requestedDateTime = requestedDateTime;
    }

    @JsonProperty("maxDateTime")
    public String getMaxDateTime() {
        return maxDateTime;
    }

    @JsonProperty("max_date_time")
    public void setMaxDateTime(String maxDateTime) {
        this.maxDateTime = maxDateTime;
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