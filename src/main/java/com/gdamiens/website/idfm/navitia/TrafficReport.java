package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class TrafficReport {

    private List<VehicleJourney> vehicleJourneys;

    @JsonProperty("lines")
    private List<Line> lines;

    @JsonProperty("network")
    private Network network;

    private List<StopArea> stopAreas;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("vehicleJourneys")
    public List<VehicleJourney> getVehicleJourneys() {
        return vehicleJourneys;
    }

    @JsonProperty("vehicle_journeys")
    public void setVehicleJourneys(List<VehicleJourney> vehicleJourneys) {
        this.vehicleJourneys = vehicleJourneys;
    }

    @JsonProperty("lines")
    public List<Line> getLines() {
        return lines;
    }

    @JsonProperty("lines")
    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    @JsonProperty("network")
    public Network getNetwork() {
        return network;
    }

    @JsonProperty("network")
    public void setNetwork(Network network) {
        this.network = network;
    }

    @JsonProperty("stopAreas")
    public List<StopArea> getStopAreas() {
        return stopAreas;
    }

    @JsonProperty("stop_areas")
    public void setStopAreas(List<StopArea> stopAreas) {
        this.stopAreas = stopAreas;
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