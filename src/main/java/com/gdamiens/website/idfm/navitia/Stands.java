package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "available_places",
    "available_bikes",
    "total_stands",
    "status"
})
@Generated("jsonschema2pojo")
public class Stands {

    private Integer availablePlaces;

    private Integer availableBikes;

    private Integer totalStands;

    @JsonProperty("status")
    private StatusEnum status;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("availablePlaces")
    public Integer getAvailablePlaces() {
        return availablePlaces;
    }

    @JsonProperty("available_places")
    public void setAvailablePlaces(Integer availablePlaces) {
        this.availablePlaces = availablePlaces;
    }

    @JsonProperty("availableBikes")
    public Integer getAvailableBikes() {
        return availableBikes;
    }

    @JsonProperty("available_bikes")
    public void setAvailableBikes(Integer availableBikes) {
        this.availableBikes = availableBikes;
    }

    @JsonProperty("totalStands")
    public Integer getTotalStands() {
        return totalStands;
    }

    @JsonProperty("total_stands")
    public void setTotalStands(Integer totalStands) {
        this.totalStands = totalStands;
    }

    @JsonProperty("status")
    public StatusEnum getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(StatusEnum status) {
        this.status = status;
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
