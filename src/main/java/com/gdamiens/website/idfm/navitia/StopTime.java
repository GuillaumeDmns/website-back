package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "arrival_time",
    "utc_arrival_time",
    "departure_time",
    "utc_departure_time",
    "headsign",
    "journey_pattern_point",
    "stop_point",
    "pickup_allowed",
    "drop_off_allowed",
    "skipped_stop"
})
@Generated("jsonschema2pojo")
public class StopTime {

    private String arrivalTime;

    private String utcArrivalTime;

    private String departureTime;

    private String utcDepartureTime;

    @JsonProperty("headsign")
    private String headsign;

    private JourneyPatternPoint journeyPatternPoint;

    private StopPoint stopPoint;

    private Boolean pickupAllowed;

    private Boolean dropOffAllowed;

    private Boolean skippedStop;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("arrivalTime")
    public String getArrivalTime() {
        return arrivalTime;
    }

    @JsonProperty("arrival_time")
    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    @JsonProperty("utcArrivalTime")
    public String getUtcArrivalTime() {
        return utcArrivalTime;
    }

    @JsonProperty("utc_arrival_time")
    public void setUtcArrivalTime(String utcArrivalTime) {
        this.utcArrivalTime = utcArrivalTime;
    }

    @JsonProperty("departureTime")
    public String getDepartureTime() {
        return departureTime;
    }

    @JsonProperty("departure_time")
    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    @JsonProperty("utcDepartureTime")
    public String getUtcDepartureTime() {
        return utcDepartureTime;
    }

    @JsonProperty("utc_departure_time")
    public void setUtcDepartureTime(String utcDepartureTime) {
        this.utcDepartureTime = utcDepartureTime;
    }

    @JsonProperty("headsign")
    public String getHeadsign() {
        return headsign;
    }

    @JsonProperty("headsign")
    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    @JsonProperty("journeyPatternPoint")
    public JourneyPatternPoint getJourneyPatternPoint() {
        return journeyPatternPoint;
    }

    @JsonProperty("journey_pattern_point")
    public void setJourneyPatternPoint(JourneyPatternPoint journeyPatternPoint) {
        this.journeyPatternPoint = journeyPatternPoint;
    }

    @JsonProperty("stopPoint")
    public StopPoint getStopPoint() {
        return stopPoint;
    }

    @JsonProperty("stop_point")
    public void setStopPoint(StopPoint stopPoint) {
        this.stopPoint = stopPoint;
    }

    @JsonProperty("pickupAllowed")
    public Boolean isPickupAllowed() {
        return pickupAllowed;
    }

    @JsonProperty("pickup_allowed")
    public void setPickupAllowed(Boolean pickupAllowed) {
        this.pickupAllowed = pickupAllowed;
    }

    @JsonProperty("dropOffAllowed")
    public Boolean isDropOffAllowed() {
        return dropOffAllowed;
    }

    @JsonProperty("drop_off_allowed")
    public void setDropOffAllowed(Boolean dropOffAllowed) {
        this.dropOffAllowed = dropOffAllowed;
    }

    @JsonProperty("skippedStop")
    public Boolean isSkippedStop() {
        return skippedStop;
    }

    @JsonProperty("skipped_stop")
    public void setSkippedStop(Boolean skippedStop) {
        this.skippedStop = skippedStop;
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
