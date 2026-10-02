package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class Dataset {

    private String realtimeLevel;

    @JsonProperty("description")
    private String description;

    @JsonProperty("system")
    private String system;

    private String startValidationDate;

    private String endValidationDate;

    @JsonProperty("contributor")
    private Contributor contributor;

    @JsonProperty("id")
    private String id;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("realtimeLevel")
    public String getRealtimeLevel() {
        return realtimeLevel;
    }

    @JsonProperty("realtime_level")
    public void setRealtimeLevel(String realtimeLevel) {
        this.realtimeLevel = realtimeLevel;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("system")
    public String getSystem() {
        return system;
    }

    @JsonProperty("system")
    public void setSystem(String system) {
        this.system = system;
    }

    @JsonProperty("startValidationDate")
    public String getStartValidationDate() {
        return startValidationDate;
    }

    @JsonProperty("start_validation_date")
    public void setStartValidationDate(String startValidationDate) {
        this.startValidationDate = startValidationDate;
    }

    @JsonProperty("endValidationDate")
    public String getEndValidationDate() {
        return endValidationDate;
    }

    @JsonProperty("end_validation_date")
    public void setEndValidationDate(String endValidationDate) {
        this.endValidationDate = endValidationDate;
    }

    @JsonProperty("contributor")
    public Contributor getContributor() {
        return contributor;
    }

    @JsonProperty("contributor")
    public void setContributor(Contributor contributor) {
        this.contributor = contributor;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
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