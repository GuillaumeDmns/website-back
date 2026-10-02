package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pagination",
    "feed_publishers",
    "disruptions",
    "origins",
    "terminus",
    "context",
    "vehicle_journeys",
    "links"
})
@Generated("jsonschema2pojo")
public class VehicleJourneys {
    @JsonProperty("pagination")
    private Pagination pagination;

    private List<FeedPublisher> feedPublishers = null;

    @JsonProperty("disruptions")
    private List<Disruption> disruptions = null;

    @JsonProperty("origins")
    private List<StopArea> origins = null;

    @JsonProperty("terminus")
    private List<StopArea> terminus = null;

    @JsonProperty("context")
    private Context context;

    private List<VehicleJourney> vehicleJourneys = null;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("pagination")
    public Pagination getPagination() {
        return pagination;
    }

    @JsonProperty("pagination")
    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    @JsonProperty("feedPublishers")
    public List<FeedPublisher> getFeedPublishers() {
        return feedPublishers;
    }

    @JsonProperty("feed_publishers")
    public void setFeedPublishers(List<FeedPublisher> feedPublishers) {
        this.feedPublishers = feedPublishers;
    }

    @JsonProperty("disruptions")
    public List<Disruption> getDisruptions() {
        return disruptions;
    }

    @JsonProperty("disruptions")
    public void setDisruptions(List<Disruption> disruptions) {
        this.disruptions = disruptions;
    }

    @JsonProperty("origins")
    public List<StopArea> getOrigins() {
        return origins;
    }

    @JsonProperty("origins")
    public void setOrigins(List<StopArea> origins) {
        this.origins = origins;
    }

    @JsonProperty("terminus")
    public List<StopArea> getTerminus() {
        return terminus;
    }

    @JsonProperty("terminus")
    public void setTerminus(List<StopArea> terminus) {
        this.terminus = terminus;
    }

    @JsonProperty("context")
    public Context getContext() {
        return context;
    }

    @JsonProperty("context")
    public void setContext(Context context) {
        this.context = context;
    }

    @JsonProperty("vehicleJourneys")
    public List<VehicleJourney> getVehicleJourneys() {
        return vehicleJourneys;
    }

    @JsonProperty("vehicle_journeys")
    public void setVehicleJourneys(List<VehicleJourney> vehicleJourneys) {
        this.vehicleJourneys = vehicleJourneys;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
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

