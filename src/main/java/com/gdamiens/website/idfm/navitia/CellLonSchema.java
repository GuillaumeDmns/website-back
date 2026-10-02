package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class CellLonSchema {

    private Double minLon;

    private Double centerLon;

    private Double maxLon;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("minLon")
    public Double getMinLon() {
        return minLon;
    }

    @JsonProperty("min_lon")
    public void setMinLon(Double minLon) {
        this.minLon = minLon;
    }

    @JsonProperty("centerLon")
    public Double getCenterLon() {
        return centerLon;
    }

    @JsonProperty("center_lon")
    public void setCenterLon(Double centerLon) {
        this.centerLon = centerLon;
    }

    @JsonProperty("maxLon")
    public Double getMaxLon() {
        return maxLon;
    }

    @JsonProperty("max_lon")
    public void setMaxLon(Double maxLon) {
        this.maxLon = maxLon;
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