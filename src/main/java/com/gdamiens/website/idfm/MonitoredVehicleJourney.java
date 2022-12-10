
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
    "LineRef",
    "OperatorRef",
    "FramedVehicleJourneyRef",
    "DirectionName",
    "DestinationRef",
    "DestinationName",
    "JourneyNote",
    "MonitoredCall"
})
@Generated("jsonschema2pojo")
public class MonitoredVehicleJourney {

    @JsonProperty("LineRef")
    private LineRef lineRef;
    @JsonProperty("OperatorRef")
    private OperatorRef operatorRef;
    @JsonProperty("FramedVehicleJourneyRef")
    private FramedVehicleJourneyRef framedVehicleJourneyRef;
    @JsonProperty("DirectionName")
    private List<DirectionName> directionName = null;
    @JsonProperty("DestinationRef")
    private DestinationRef destinationRef;
    @JsonProperty("DestinationName")
    private List<DestinationName> destinationName = null;
    @JsonProperty("JourneyNote")
    private List<JourneyNote> journeyNote = null;
    @JsonProperty("MonitoredCall")
    private MonitoredCall monitoredCall;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("LineRef")
    public LineRef getLineRef() {
        return lineRef;
    }

    @JsonProperty("LineRef")
    public void setLineRef(LineRef lineRef) {
        this.lineRef = lineRef;
    }

    @JsonProperty("OperatorRef")
    public OperatorRef getOperatorRef() {
        return operatorRef;
    }

    @JsonProperty("OperatorRef")
    public void setOperatorRef(OperatorRef operatorRef) {
        this.operatorRef = operatorRef;
    }

    @JsonProperty("FramedVehicleJourneyRef")
    public FramedVehicleJourneyRef getFramedVehicleJourneyRef() {
        return framedVehicleJourneyRef;
    }

    @JsonProperty("FramedVehicleJourneyRef")
    public void setFramedVehicleJourneyRef(FramedVehicleJourneyRef framedVehicleJourneyRef) {
        this.framedVehicleJourneyRef = framedVehicleJourneyRef;
    }

    @JsonProperty("DirectionName")
    public List<DirectionName> getDirectionName() {
        return directionName;
    }

    @JsonProperty("DirectionName")
    public void setDirectionName(List<DirectionName> directionName) {
        this.directionName = directionName;
    }

    @JsonProperty("DestinationRef")
    public DestinationRef getDestinationRef() {
        return destinationRef;
    }

    @JsonProperty("DestinationRef")
    public void setDestinationRef(DestinationRef destinationRef) {
        this.destinationRef = destinationRef;
    }

    @JsonProperty("DestinationName")
    public List<DestinationName> getDestinationName() {
        return destinationName;
    }

    @JsonProperty("DestinationName")
    public void setDestinationName(List<DestinationName> destinationName) {
        this.destinationName = destinationName;
    }

    @JsonProperty("JourneyNote")
    public List<JourneyNote> getJourneyNote() {
        return journeyNote;
    }

    @JsonProperty("JourneyNote")
    public void setJourneyNote(List<JourneyNote> journeyNote) {
        this.journeyNote = journeyNote;
    }

    @JsonProperty("MonitoredCall")
    public MonitoredCall getMonitoredCall() {
        return monitoredCall;
    }

    @JsonProperty("MonitoredCall")
    public void setMonitoredCall(MonitoredCall monitoredCall) {
        this.monitoredCall = monitoredCall;
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
