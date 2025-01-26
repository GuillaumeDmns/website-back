package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "from",
    "type"
})
@Generated("jsonschema2pojo")
public class ImpactedSection {

    @JsonProperty("from")
    private PtObject from;

    @JsonProperty("to")
    private PtObject to;

    @JsonProperty("routes")
    private List<Route> routes = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("from")
    public PtObject getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(PtObject from) {
        this.from = from;
    }

    @JsonProperty("to")
    public PtObject getTo() {
        return to;
    }

    @JsonProperty("to")
    public void setTo(PtObject to) {
        this.to = to;
    }

    @JsonProperty("routes")
    public List<Route> getRoutes() {
        return routes;
    }

    @JsonProperty("routes")
    public void setRoutes(List<Route> routes) {
        this.routes = routes;
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
