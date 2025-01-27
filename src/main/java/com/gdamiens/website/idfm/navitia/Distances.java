package com.gdamiens.website.idfm.navitia;

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
    "taxi",
    "car",
    "walking",
    "bike",
    "ridesharing"
})
@Generated("jsonschema2pojo")
public class Distances {

    @JsonProperty("taxi")
    private Integer taxi;

    @JsonProperty("car")
    private Integer car;

    @JsonProperty("walking")
    private Integer walking;

    @JsonProperty("bike")
    private Integer bike;

    @JsonProperty("ridesharing")
    private Integer ridesharing;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("taxi")
    public Integer getTaxi() {
        return taxi;
    }

    @JsonProperty("taxi")
    public void setTaxi(Integer taxi) {
        this.taxi = taxi;
    }

    @JsonProperty("car")
    public Integer getCar() {
        return car;
    }

    @JsonProperty("car")
    public void setCar(Integer car) {
        this.car = car;
    }

    @JsonProperty("walking")
    public Integer getWalking() {
        return walking;
    }

    @JsonProperty("walking")
    public void setWalking(Integer walking) {
        this.walking = walking;
    }

    @JsonProperty("bike")
    public Integer getBike() {
        return bike;
    }

    @JsonProperty("bike")
    public void setBike(Integer bike) {
        this.bike = bike;
    }

    @JsonProperty("ridesharing")
    public Integer getRidesharing() {
        return ridesharing;
    }

    @JsonProperty("ridesharing")
    public void setRidesharing(Integer ridesharing) {
        this.ridesharing = ridesharing;
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
