package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class IndividualRating {

    @JsonProperty("count")
    private Integer count;

    private Double scaleMin;

    private Double scaleMax;

    @JsonProperty("value")
    private Double value;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    @JsonProperty("scaleMin")
    public Double getScaleMin() {
        return scaleMin;
    }

    @JsonProperty("scale_min")
    public void setScaleMin(Double scaleMin) {
        this.scaleMin = scaleMin;
    }

    @JsonProperty("scaleMax")
    public Double getScaleMax() {
        return scaleMax;
    }

    @JsonProperty("scale_max")
    public void setScaleMax(Double scaleMax) {
        this.scaleMax = scaleMax;
    }

    @JsonProperty("value")
    public Double getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(Double value) {
        this.value = value;
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