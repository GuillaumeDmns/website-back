package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "journey_pattern",
    "stop_times",
    "codes",
    "validity_pattern",
    "calendars",
    "trip",
    "disruptions",
    "headsign"
})
@Generated("jsonschema2pojo")
public class VehicleJourney {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("journey_pattern")
    private JourneyPattern journeyPattern;

    @JsonProperty("stop_times")
    private List<StopTime> stopTimes = null;

    @JsonProperty("codes")
    private List<Code> codes = null;

    @JsonProperty("validity_pattern")
    private ValidityPattern validityPattern;

    @JsonProperty("calendars")
    private List<Calendar> calendars = null;

    @JsonProperty("trip")
    private Trip trip;

    @JsonProperty("disruptions")
    private List<Disruption> disruptions = null;

    @JsonProperty("headsign")
    private String headsign;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("journey_pattern")
    public JourneyPattern getJourneyPattern() {
        return journeyPattern;
    }

    @JsonProperty("journey_pattern")
    public void setJourneyPattern(JourneyPattern journeyPattern) {
        this.journeyPattern = journeyPattern;
    }

    @JsonProperty("stop_times")
    public List<StopTime> getStopTimes() {
        return stopTimes;
    }

    @JsonProperty("stop_times")
    public void setStopTimes(List<StopTime> stopTimes) {
        this.stopTimes = stopTimes;
    }

    @JsonProperty("codes")
    public List<Code> getCodes() {
        return codes;
    }

    @JsonProperty("codes")
    public void setCodes(List<Code> codes) {
        this.codes = codes;
    }

    @JsonProperty("validity_pattern")
    public ValidityPattern getValidityPattern() {
        return validityPattern;
    }

    @JsonProperty("validity_pattern")
    public void setValidityPattern(ValidityPattern validityPattern) {
        this.validityPattern = validityPattern;
    }

    @JsonProperty("calendars")
    public List<Calendar> getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    public void setCalendars(List<Calendar> calendars) {
        this.calendars = calendars;
    }

    @JsonProperty("trip")
    public Trip getTrip() {
        return trip;
    }

    @JsonProperty("trip")
    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    @JsonProperty("disruptions")
    public List<Disruption> getDisruptions() {
        return disruptions;
    }

    @JsonProperty("disruptions")
    public void setDisruptions(List<Disruption> disruptions) {
        this.disruptions = disruptions;
    }

    @JsonProperty("headsign")
    public String getHeadsign() {
        return headsign;
    }

    @JsonProperty("headsign")
    public void setHeadsign(String headsign) {
        this.headsign = headsign;
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
