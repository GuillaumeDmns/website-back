
package com.gdamiens.website.idfm;

import java.util.HashMap;
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
    "RecordedAtTime",
    "ItemIdentifier",
    "MonitoringRef",
    "MonitoredVehicleJourney"
})
@Generated("jsonschema2pojo")
public class MonitoredStopVisit {

    @JsonProperty("RecordedAtTime")
    private String recordedAtTime;
    @JsonProperty("ItemIdentifier")
    private String itemIdentifier;
    @JsonProperty("MonitoringRef")
    private MonitoringRef monitoringRef;
    @JsonProperty("MonitoredVehicleJourney")
    private MonitoredVehicleJourney monitoredVehicleJourney;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("RecordedAtTime")
    public String getRecordedAtTime() {
        return recordedAtTime;
    }

    @JsonProperty("RecordedAtTime")
    public void setRecordedAtTime(String recordedAtTime) {
        this.recordedAtTime = recordedAtTime;
    }

    @JsonProperty("ItemIdentifier")
    public String getItemIdentifier() {
        return itemIdentifier;
    }

    @JsonProperty("ItemIdentifier")
    public void setItemIdentifier(String itemIdentifier) {
        this.itemIdentifier = itemIdentifier;
    }

    @JsonProperty("MonitoringRef")
    public MonitoringRef getMonitoringRef() {
        return monitoringRef;
    }

    @JsonProperty("MonitoringRef")
    public void setMonitoringRef(MonitoringRef monitoringRef) {
        this.monitoringRef = monitoringRef;
    }

    @JsonProperty("MonitoredVehicleJourney")
    public MonitoredVehicleJourney getMonitoredVehicleJourney() {
        return monitoredVehicleJourney;
    }

    @JsonProperty("MonitoredVehicleJourney")
    public void setMonitoredVehicleJourney(MonitoredVehicleJourney monitoredVehicleJourney) {
        this.monitoredVehicleJourney = monitoredVehicleJourney;
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
