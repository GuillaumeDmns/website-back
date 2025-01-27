package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "amended_arrival_time",
    "stop_point",
    "stop_time_effect",
    "departure_status",
    "is_detour",
    "amended_departure_time",
    "base_arrival_time",
    "cause",
    "base_departure_time",
    "arrival_status"
})
@Generated("jsonschema2pojo")
public class ImpactedStop {

    private String amendedArrivalTime;

    private StopPoint stopPoint;

    private StopTimeEffectEnum stopTimeEffect;

    private String departureStatus;

    private Boolean isDetour;

    private String amendedDepartureTime;

    private String baseArrivalTime;

    @JsonProperty("cause")
    private String cause;

    private String baseDepartureTime;

    private String arrivalStatus;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("amendedArrivalTime")
    public String getAmendedArrivalTime() {
        return amendedArrivalTime;
    }

    @JsonProperty("amended_arrival_time")
    public void setAmendedArrivalTime(String amendedArrivalTime) {
        this.amendedArrivalTime = amendedArrivalTime;
    }

    @JsonProperty("stopPoint")
    public StopPoint getStopPoint() {
        return stopPoint;
    }

    @JsonProperty("stop_point")
    public void setStopPoint(StopPoint stopPoint) {
        this.stopPoint = stopPoint;
    }

    @JsonProperty("stopTimeEffect")
    public StopTimeEffectEnum getStopTimeEffect() {
        return stopTimeEffect;
    }

    @JsonProperty("stop_time_effect")
    public void setStopTimeEffect(StopTimeEffectEnum stopTimeEffect) {
        this.stopTimeEffect = stopTimeEffect;
    }

    @JsonProperty("departureStatus")
    public String getDepartureStatus() {
        return departureStatus;
    }

    @JsonProperty("departure_status")
    public void setDepartureStatus(String departureStatus) {
        this.departureStatus = departureStatus;
    }

    @JsonProperty("isDetour")
    public Boolean getDetour() {
        return isDetour;
    }

    @JsonProperty("is_detour")
    public void setDetour(Boolean detour) {
        isDetour = detour;
    }

    @JsonProperty("amendedDepartureTime")
    public String getAmendedDepartureTime() {
        return amendedDepartureTime;
    }

    @JsonProperty("amended_departure_time")
    public void setAmendedDepartureTime(String amendedDepartureTime) {
        this.amendedDepartureTime = amendedDepartureTime;
    }

    @JsonProperty("baseArrivalTime")
    public String getBaseArrivalTime() {
        return baseArrivalTime;
    }

    @JsonProperty("base_arrival_time")
    public void setBaseArrivalTime(String baseArrivalTime) {
        this.baseArrivalTime = baseArrivalTime;
    }

    @JsonProperty("cause")
    public String getCause() {
        return cause;
    }

    @JsonProperty("cause")
    public void setCause(String cause) {
        this.cause = cause;
    }

    @JsonProperty("baseDepartureTime")
    public String getBaseDepartureTime() {
        return baseDepartureTime;
    }

    @JsonProperty("base_departure_time")
    public void setBaseDepartureTime(String baseDepartureTime) {
        this.baseDepartureTime = baseDepartureTime;
    }

    @JsonProperty("arrivalStatus")
    public String getArrivalStatus() {
        return arrivalStatus;
    }

    @JsonProperty("arrival_status")
    public void setArrivalStatus(String arrivalStatus) {
        this.arrivalStatus = arrivalStatus;
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
