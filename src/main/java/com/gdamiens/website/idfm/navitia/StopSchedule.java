package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class StopSchedule {

    private StopPoint stopPoint;

    @JsonProperty("links")
    private List<LinkSchema> links;

    private List<DateTimeType> dateTimes;

    @JsonProperty("route")
    private Route route;

    private String additionalInformations;

    private VJDisplayInformation displayInformations;

    private String lastDatetime;

    private String firstDatetime;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("stopPoint")
    public StopPoint getStopPoint() {
        return stopPoint;
    }

    @JsonProperty("stop_point")
    public void setStopPoint(StopPoint stopPoint) {
        this.stopPoint = stopPoint;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
    }

    @JsonProperty("dateTimes")
    public List<DateTimeType> getDateTimes() {
        return dateTimes;
    }

    @JsonProperty("date_times")
    public void setDateTimes(List<DateTimeType> dateTimes) {
        this.dateTimes = dateTimes;
    }

    @JsonProperty("route")
    public Route getRoute() {
        return route;
    }

    @JsonProperty("route")
    public void setRoute(Route route) {
        this.route = route;
    }

    @JsonProperty("additionalInformations")
    public String getAdditionalInformations() {
        return additionalInformations;
    }

    @JsonProperty("additional_informations")
    public void setAdditionalInformations(String additionalInformations) {
        this.additionalInformations = additionalInformations;
    }

    @JsonProperty("displayInformations")
    public VJDisplayInformation getDisplayInformations() {
        return displayInformations;
    }

    @JsonProperty("display_informations")
    public void setDisplayInformations(VJDisplayInformation displayInformations) {
        this.displayInformations = displayInformations;
    }

    @JsonProperty("lastDatetime")
    public String getLastDatetime() {
        return lastDatetime;
    }

    @JsonProperty("last_datetime")
    public void setLastDatetime(String lastDatetime) {
        this.lastDatetime = lastDatetime;
    }

    @JsonProperty("firstDatetime")
    public String getFirstDatetime() {
        return firstDatetime;
    }

    @JsonProperty("first_datetime")
    public void setFirstDatetime(String firstDatetime) {
        this.firstDatetime = firstDatetime;
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