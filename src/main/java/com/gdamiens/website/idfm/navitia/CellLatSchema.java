package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class CellLatSchema {

    private Double minLat;

    private Double maxLat;

    private Double centerLat;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("minLat")
    public Double getMinLat() {
        return minLat;
    }

    @JsonProperty("min_lat")
    public void setMinLat(Double minLat) {
        this.minLat = minLat;
    }

    @JsonProperty("maxLat")
    public Double getMaxLat() {
        return maxLat;
    }

    @JsonProperty("max_lat")
    public void setMaxLat(Double maxLat) {
        this.maxLat = maxLat;
    }

    @JsonProperty("centerLat")
    public Double getCenterLat() {
        return centerLat;
    }

    @JsonProperty("center_lat")
    public void setCenterLat(Double centerLat) {
        this.centerLat = centerLat;
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