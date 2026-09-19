package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class Header {

    private VJDisplayInformation displayInformations;

    private List<String> additionalInformations;

    @JsonProperty("links")
    private List<LinkSchema> links;

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

    @JsonProperty("additionalInformations")
    public List<String> getAdditionalInformations() {
        return additionalInformations;
    }

    @JsonProperty("additional_informations")
    public void setAdditionalInformations(List<String> additionalInformations) {
        this.additionalInformations = additionalInformations;
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