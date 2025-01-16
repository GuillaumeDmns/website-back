package com.gdamiens.website.idfm;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ExpectedQuayRef"
})
@Generated("jsonschema2pojo")
public class DepartureStopAssignment {

    @JsonProperty("ExpectedQuayRef")
    private ExpectedQuayRef expectedQuayRef;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ExpectedQuayRef")
    public ExpectedQuayRef getExpectedQuayRef() {
        return expectedQuayRef;
    }

    @JsonProperty("ExpectedQuayRef")
    public void setExpectedQuayRef(ExpectedQuayRef expectedQuayRef) {
        this.expectedQuayRef = expectedQuayRef;
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
