package com.gdamiens.website.client.siri;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** A call already made by a vehicle journey; other tags (statuses, platforms) are kept as additional properties */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "StopPointRef",
    "Order",
    "AimedArrivalTime",
    "ActualArrivalTime",
    "AimedDepartureTime",
    "ExpectedDepartureTime",
    "ActualDepartureTime"
})
public class RecordedCall {

    @JsonProperty("StopPointRef")
    private StopPointRef stopPointRef;
    @JsonProperty("Order")
    private Integer order;
    @JsonProperty("AimedArrivalTime")
    private String aimedArrivalTime;
    @JsonProperty("ActualArrivalTime")
    private String actualArrivalTime;
    @JsonProperty("AimedDepartureTime")
    private String aimedDepartureTime;
    @JsonProperty("ExpectedDepartureTime")
    private String expectedDepartureTime;
    @JsonProperty("ActualDepartureTime")
    private String actualDepartureTime;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("StopPointRef")
    public StopPointRef getStopPointRef() {
        return stopPointRef;
    }

    @JsonProperty("StopPointRef")
    public void setStopPointRef(StopPointRef stopPointRef) {
        this.stopPointRef = stopPointRef;
    }

    @JsonProperty("Order")
    public Integer getOrder() {
        return order;
    }

    @JsonProperty("Order")
    public void setOrder(Integer order) {
        this.order = order;
    }

    @JsonProperty("AimedArrivalTime")
    public String getAimedArrivalTime() {
        return aimedArrivalTime;
    }

    @JsonProperty("AimedArrivalTime")
    public void setAimedArrivalTime(String aimedArrivalTime) {
        this.aimedArrivalTime = aimedArrivalTime;
    }

    @JsonProperty("ActualArrivalTime")
    public String getActualArrivalTime() {
        return actualArrivalTime;
    }

    @JsonProperty("ActualArrivalTime")
    public void setActualArrivalTime(String actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
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

    @JsonProperty("ActualDepartureTime")
    public String getActualDepartureTime() {
        return actualDepartureTime;
    }

    @JsonProperty("ActualDepartureTime")
    public void setActualDepartureTime(String actualDepartureTime) {
        this.actualDepartureTime = actualDepartureTime;
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
