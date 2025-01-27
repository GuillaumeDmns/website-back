package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "found",
    "total",
    "links"
})
@Generated("jsonschema2pojo")
public class Fare {

    @JsonProperty("found")
    private Boolean found;

    @JsonProperty("total")
    private Cost total;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("found")
    public Boolean getFound() {
        return found;
    }

    @JsonProperty("found")
    public void setFound(Boolean found) {
        this.found = found;
    }

    @JsonProperty("total")
    public Cost getTotal() {
        return total;
    }

    @JsonProperty("total")
    public void setTotal(Cost total) {
        this.total = total;
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
