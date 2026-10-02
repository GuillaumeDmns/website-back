package com.gdamiens.website.idfm.navitia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pagination",
    "disruptions",
    "placesNearby",
    "context",
    "links"
})
public class PlacesNearby {

    @JsonProperty("pagination")
    private Pagination pagination;

    @JsonProperty("disruptions")
    private List<Disruption> disruptions = null;

    private List<PlaceNearby> placesNearby = null;

    @JsonProperty("context")
    private Context context;

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

    @JsonProperty("disruptions")
    public List<Disruption> getDisruptions() {
        return disruptions;
    }

    @JsonProperty("disruptions")
    public void setDisruptions(List<Disruption> disruptions) {
        this.disruptions = disruptions;
    }

    @JsonProperty("placesNearby")
    public List<PlaceNearby> getPlacesNearby() {
        return placesNearby;
    }

    @JsonProperty("places_nearby")
    public void setPlacesNearby(List<PlaceNearby> placesNearby) {
        this.placesNearby = placesNearby;
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
