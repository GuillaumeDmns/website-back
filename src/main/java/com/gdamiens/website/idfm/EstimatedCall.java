
package com.gdamiens.website.idfm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "StopPointRef",
    "ExpectedArrivalTime",
    "ExpectedDepartureTime",
    "AimedArrivalTime",
    "AimedDepartureTime",
    "DestinationDisplay",
    "ArrivalStatus",
    "ArrivalProximityText",
    "ArrivalPlatformName",
    "DepartureStatus"
})
@Generated("jsonschema2pojo")
public class EstimatedCall {

    @JsonProperty("StopPointRef")
    private StopPointRef stopPointRef;
    @JsonProperty("ExpectedArrivalTime")
    private String expectedArrivalTime;
    @JsonProperty("ExpectedDepartureTime")
    private String expectedDepartureTime;
    @JsonProperty("AimedArrivalTime")
    private String aimedArrivalTime;
    @JsonProperty("AimedDepartureTime")
    private String aimedDepartureTime;
    @JsonProperty("DestinationDisplay")
    private List<DestinationDisplay> destinationDisplay = null;
    @JsonProperty("ArrivalStatus")
    private String arrivalStatus;
    @JsonProperty("ArrivalProximityText")
    private ArrivalProximityText arrivalProximityText;
    @JsonProperty("ArrivalPlatformName")
    private ArrivalPlatformName arrivalPlatformName;
    @JsonProperty("DepartureStatus")
    private DepartureStatusEnum departureStatus;

    // Other properties for processing
    private String recordedAtTime;
    private String journeyNote;
    private FirstOrLastJourneyEnum firstOrLastJourney;

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

    @JsonProperty("ExpectedArrivalTime")
    public String getExpectedArrivalTime() {
        return expectedArrivalTime;
    }

    @JsonProperty("ExpectedArrivalTime")
    public void setExpectedArrivalTime(String expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    @JsonProperty("ExpectedDepartureTime")
    public String getExpectedDepartureTime() {
        return expectedDepartureTime;
    }

    @JsonProperty("ExpectedDepartureTime")
    public void setExpectedDepartureTime(String expectedDepartureTime) {
        this.expectedDepartureTime = expectedDepartureTime;
    }

    @JsonProperty("AimedArrivalTime")
    public String getAimedArrivalTime() {
        return aimedArrivalTime;
    }

    @JsonProperty("AimedArrivalTime")
    public void setAimedArrivalTime(String aimedArrivalTime) {
        this.aimedArrivalTime = aimedArrivalTime;
    }

    @JsonProperty("AimedDepartureTime")
    public String getAimedDepartureTime() {
        return aimedDepartureTime;
    }

    @JsonProperty("AimedDepartureTime")
    public void setAimedDepartureTime(String aimedDepartureTime) {
        this.aimedDepartureTime = aimedDepartureTime;
    }

    @JsonProperty("DestinationDisplay")
    public List<DestinationDisplay> getDestinationDisplay() {
        return destinationDisplay;
    }

    @JsonProperty("DestinationDisplay")
    public void setDestinationDisplay(List<DestinationDisplay> destinationDisplay) {
        this.destinationDisplay = destinationDisplay;
    }

    @JsonProperty("ArrivalStatus")
    public String getArrivalStatus() {
        return arrivalStatus;
    }

    @JsonProperty("ArrivalStatus")
    public void setArrivalStatus(String arrivalStatus) {
        this.arrivalStatus = arrivalStatus;
    }

    @JsonProperty("ArrivalProximityText")
    public ArrivalProximityText getArrivalProximityText() {
        return arrivalProximityText;
    }

    @JsonProperty("ArrivalProximityText")
    public void setArrivalProximityText(ArrivalProximityText arrivalProximityText) {
        this.arrivalProximityText = arrivalProximityText;
    }

    @JsonProperty("ArrivalPlatformName")
    public ArrivalPlatformName getArrivalPlatformName() {
        return arrivalPlatformName;
    }

    @JsonProperty("ArrivalPlatformName")
    public void setArrivalPlatformName(ArrivalPlatformName arrivalPlatformName) {
        this.arrivalPlatformName = arrivalPlatformName;
    }

    @JsonProperty("DepartureStatus")
    public DepartureStatusEnum getDepartureStatus() {
        return departureStatus;
    }

    @JsonProperty("DepartureStatus")
    public void setDepartureStatus(DepartureStatusEnum departureStatus) {
        this.departureStatus = departureStatus;
    }

    public String getRecordedAtTime() {
        return recordedAtTime;
    }

    public void setRecordedAtTime(String recordedAtTime) {
        this.recordedAtTime = recordedAtTime;
    }

    public String getJourneyNote() {
        return journeyNote;
    }

    public void setJourneyNote(String journeyNote) {
        this.journeyNote = journeyNote;
    }

    public FirstOrLastJourneyEnum getFirstOrLastJourney() {
        return firstOrLastJourney;
    }

    public void setFirstOrLastJourney(FirstOrLastJourneyEnum firstOrLastJourney) {
        this.firstOrLastJourney = firstOrLastJourney;
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
