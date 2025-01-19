package com.gdamiens.website.idfm.navitia;

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
    "feedPublishers",
    "disruptions",
    "places",
    "context",
    "links"
})
@Generated("jsonschema2pojo")
public class Places {

    @JsonProperty("feed_publishers")
    private List<FeedPublisher> feedPublishers = null;

    @JsonProperty("places")
    private List<Place> places = null;

    @JsonProperty("context")
    private Context context;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    @JsonProperty("feed_publishers")
    public List<FeedPublisher> getFeedPublishers() {
        return feedPublishers;
    }

    @JsonProperty("feed_publishers")
    public void setFeedPublishers(List<FeedPublisher> feedPublishers) {
        this.feedPublishers = feedPublishers;
    }

    @JsonProperty("places")
    public List<Place> getPlaces() {
        return places;
    }

    @JsonProperty("places")
    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    @JsonProperty("context")
    public Context getContext() {
        return context;
    }

    @JsonProperty("context")
    public void setContext(Context context) {
        this.context = context;
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
