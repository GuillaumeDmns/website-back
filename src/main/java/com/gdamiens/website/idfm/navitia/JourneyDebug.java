package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "nb_vj_extentions",
    "nb_sections",
    "internal_id",
    "streetnetwork_duration",
    "transfer_duration",
    "min_waiting_duration"
})
@Generated("jsonschema2pojo")
public class JourneyDebug {

    private Integer nbVjExtentions;

    private Integer nbSections;

    private String internalId;

    private Integer streetnetworkDuration;

    private Integer transferDuration;

    private Integer minWaitingDuration;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("nbVjExtentions")
    public Integer getNbVjExtentions() {
        return nbVjExtentions;
    }

    @JsonProperty("nb_vj_extentions")
    public void setNbVjExtentions(Integer nbVjExtentions) {
        this.nbVjExtentions = nbVjExtentions;
    }

    @JsonProperty("nbSections")
    public Integer getNbSections() {
        return nbSections;
    }

    @JsonProperty("nb_sections")
    public void setNbSections(Integer nbSections) {
        this.nbSections = nbSections;
    }

    @JsonProperty("internalId")
    public String getInternalId() {
        return internalId;
    }

    @JsonProperty("internal_id")
    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    @JsonProperty("streetnetworkDuration")
    public Integer getStreetnetworkDuration() {
        return streetnetworkDuration;
    }

    @JsonProperty("streetnetwork_duration")
    public void setStreetnetworkDuration(Integer streetnetworkDuration) {
        this.streetnetworkDuration = streetnetworkDuration;
    }

    @JsonProperty("transferDuration")
    public Integer getTransferDuration() {
        return transferDuration;
    }

    @JsonProperty("transfer_duration")
    public void setTransferDuration(Integer transferDuration) {
        this.transferDuration = transferDuration;
    }

    @JsonProperty("minWaitingDuration")
    public Integer getMinWaitingDuration() {
        return minWaitingDuration;
    }

    @JsonProperty("min_waiting_duration")
    public void setMinWaitingDuration(Integer minWaitingDuration) {
        this.minWaitingDuration = minWaitingDuration;
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
