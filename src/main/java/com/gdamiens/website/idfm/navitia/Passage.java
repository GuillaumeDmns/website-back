package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class Passage {

    private VJDisplayInformation displayInformations;

    private StopPoint stopPoint;

    @JsonProperty("route")
    private Route route;

    @JsonProperty("links")
    private List<LinkSchema> links;

    private StopDateTime stopDateTime;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("displayInformations")
    public VJDisplayInformation getDisplayInformations() {
        return displayInformations;
    }

    @JsonProperty("display_informations")
    public void setDisplayInformations(VJDisplayInformation displayInformations) {
        this.displayInformations = displayInformations;
    }

    @JsonProperty("stopPoint")
    public StopPoint getStopPoint() {
        return stopPoint;
    }

    @JsonProperty("stop_point")
    public void setStopPoint(StopPoint stopPoint) {
        this.stopPoint = stopPoint;
    }

    @JsonProperty("route")
    public Route getRoute() {
        return route;
    }

    @JsonProperty("route")
    public void setRoute(Route route) {
        this.route = route;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
    }

    @JsonProperty("stopDateTime")
    public StopDateTime getStopDateTime() {
        return stopDateTime;
    }

    @JsonProperty("stop_date_time")
    public void setStopDateTime(StopDateTime stopDateTime) {
        this.stopDateTime = stopDateTime;
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