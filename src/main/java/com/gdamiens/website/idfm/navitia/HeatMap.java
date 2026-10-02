package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class HeatMap {

    @JsonProperty("to")
    private Place to;

    private String requestedDateTime;

    @JsonProperty("from")
    private Place from;

    private HeatMatrixSchema heatMatrix;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

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

    @JsonProperty("from")
    public Place getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(Place from) {
        this.from = from;
    }

    @JsonProperty("heatMatrix")
    public HeatMatrixSchema getHeatMatrix() {
        return heatMatrix;
    }

    @JsonProperty("heat_matrix")
    public void setHeatMatrix(HeatMatrixSchema heatMatrix) {
        this.heatMatrix = heatMatrix;
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