
package com.gdamiens.website.idfm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "Order",
    "StopPointName",
    "VehicleAtStop",
    "DestinationDisplay",
    "AimedArrivalTime",
    "ExpectedArrivalTime",
    "ArrivalPlatformName",
    "AimedDepartureTime",
    "ExpectedDepartureTime",
    "ArrivalStatus",
    "DepartureStatus"
})
@Generated("jsonschema2pojo")
public class MonitoredCall {

    @JsonProperty("Order")
    private Integer order;
    @JsonProperty("StopPointName")
    private List<StopPointName> stopPointName = null;
    @JsonProperty("VehicleAtStop")
    private Boolean vehicleAtStop;
    @JsonProperty("DestinationDisplay")
    private List<DestinationDisplay> destinationDisplay = null;
    @JsonProperty("AimedArrivalTime")
    private String aimedArrivalTime;
    @JsonProperty("ExpectedArrivalTime")
    private String expectedArrivalTime;
    @JsonProperty("ArrivalPlatformName")
    private ArrivalPlatformName arrivalPlatformName;
    @JsonProperty("AimedDepartureTime")
    private String aimedDepartureTime;
    @JsonProperty("ExpectedDepartureTime")
    private String expectedDepartureTime;
    @JsonProperty("ArrivalStatus")
    private DepartureStatusEnum arrivalStatus;
    @JsonProperty("DepartureStatus")
    private DepartureStatusEnum departureStatus;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("Order")
    public Integer getOrder() {
        return order;
    }

    @JsonProperty("Order")
    public void setOrder(Integer order) {
        this.order = order;
    }

    @JsonProperty("StopPointName")
    public List<StopPointName> getStopPointName() {
        return stopPointName;
    }

    @JsonProperty("StopPointName")
    public void setStopPointName(List<StopPointName> stopPointName) {
        this.stopPointName = stopPointName;
    }

    @JsonProperty("VehicleAtStop")
    public Boolean getVehicleAtStop() {
        return vehicleAtStop;
    }

    @JsonProperty("VehicleAtStop")
    public void setVehicleAtStop(Boolean vehicleAtStop) {
        this.vehicleAtStop = vehicleAtStop;
    }

    @JsonProperty("DestinationDisplay")
    public List<DestinationDisplay> getDestinationDisplay() {
        return destinationDisplay;
    }

    @JsonProperty("DestinationDisplay")
    public void setDestinationDisplay(List<DestinationDisplay> destinationDisplay) {
        this.destinationDisplay = destinationDisplay;
    }

    @JsonProperty("AimedArrivalTime")
    public String getAimedArrivalTime() {
        return aimedArrivalTime;
    }

    @JsonProperty("AimedArrivalTime")
    public void setAimedArrivalTime(String aimedArrivalTime) {
        this.aimedArrivalTime = aimedArrivalTime;
    }

    @JsonProperty("ExpectedArrivalTime")
    public String getExpectedArrivalTime() {
        return expectedArrivalTime;
    }

    @JsonProperty("ExpectedArrivalTime")
    public void setExpectedArrivalTime(String expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    @JsonProperty("ArrivalPlatformName")
    public ArrivalPlatformName getArrivalPlatformName() {
        return arrivalPlatformName;
    }

    @JsonProperty("ArrivalPlatformName")
    public void setArrivalPlatformName(ArrivalPlatformName arrivalPlatformName) {
        this.arrivalPlatformName = arrivalPlatformName;
    }

    @JsonProperty("AimedDepartureTime")
    public String getAimedDepartureTime() {
        return aimedDepartureTime;
    }

    @JsonProperty("AimedDepartureTime")
    public void setAimedDepartureTime(String aimedDepartureTime) {
        this.aimedDepartureTime = aimedDepartureTime;
    }

    @JsonProperty("ExpectedDepartureTime")
    public String getExpectedDepartureTime() {
        return expectedDepartureTime;
    }

    @JsonProperty("ExpectedDepartureTime")
    public void setExpectedDepartureTime(String expectedDepartureTime) {
        this.expectedDepartureTime = expectedDepartureTime;
    }

    @JsonProperty("ArrivalStatus")
    public DepartureStatusEnum getArrivalStatus() {
        return arrivalStatus;
    }

    @JsonProperty("ArrivalStatus")
    public void setArrivalStatus(DepartureStatusEnum arrivalStatus) {
        this.arrivalStatus = arrivalStatus;
    }

    @JsonProperty("DepartureStatus")
    public DepartureStatusEnum getDepartureStatus() {
        return departureStatus;
    }

    @JsonProperty("DepartureStatus")
    public void setDepartureStatus(DepartureStatusEnum departureStatus) {
        this.departureStatus = departureStatus;
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
