package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class CurrentAvailability {

    @JsonProperty("status")
    private String status;

    @JsonProperty("effect")
    private Effect effect;

    @JsonProperty("cause")
    private Cause cause;

    @JsonProperty("periods")
    private List<Period> periods;

    private String updatedAt;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("effect")
    public Effect getEffect() {
        return effect;
    }

    @JsonProperty("effect")
    public void setEffect(Effect effect) {
        this.effect = effect;
    }

    @JsonProperty("cause")
    public Cause getCause() {
        return cause;
    }

    @JsonProperty("cause")
    public void setCause(Cause cause) {
        this.cause = cause;
    }

    @JsonProperty("periods")
    public List<Period> getPeriods() {
        return periods;
    }

    @JsonProperty("periods")
    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    @JsonProperty("updatedAt")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
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