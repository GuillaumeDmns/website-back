package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pt_object",
    "impacted_stops",
    "impacted_section",
    "impacted_rail_section"
})
@Generated("jsonschema2pojo")
public class Impacted {

    private PtObject ptObject;

    private List<ImpactedStop> impactedStops = null;

    private ImpactedSection impactedSection;

    private ImpactedSection impactedRailSection;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ptObject")
    public PtObject getPtObject() {
        return ptObject;
    }

    @JsonProperty("pt_object")
    public void setPtObject(PtObject ptObject) {
        this.ptObject = ptObject;
    }

    @JsonProperty("impactedStops")
    public List<ImpactedStop> getImpactedStops() {
        return impactedStops;
    }

    @JsonProperty("impacted_stops")
    public void setImpactedStops(List<ImpactedStop> impactedStops) {
        this.impactedStops = impactedStops;
    }

    @JsonProperty("impactedSection")
    public ImpactedSection getImpactedSection() {
        return impactedSection;
    }

    @JsonProperty("impacted_section")
    public void setImpactedSection(ImpactedSection impactedSection) {
        this.impactedSection = impactedSection;
    }

    @JsonProperty("impactedRailSection")
    public ImpactedSection getImpactedRailSection() {
        return impactedRailSection;
    }

    @JsonProperty("impacted_rail_section")
    public void setImpactedRailSection(ImpactedSection impactedRailSection) {
        this.impactedRailSection = impactedRailSection;
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
