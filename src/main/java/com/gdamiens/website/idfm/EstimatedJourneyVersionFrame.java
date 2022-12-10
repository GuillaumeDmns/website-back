
package com.gdamiens.website.idfm;

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
    "EstimatedVehicleJourney"
})
@Generated("jsonschema2pojo")
public class EstimatedJourneyVersionFrame {

    @JsonProperty("EstimatedVehicleJourney")
    private List<EstimatedVehicleJourney> estimatedVehicleJourney = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("EstimatedVehicleJourney")
    public List<EstimatedVehicleJourney> getEstimatedVehicleJourney() {
        return estimatedVehicleJourney;
    }

    @JsonProperty("EstimatedVehicleJourney")
    public void setEstimatedVehicleJourney(List<EstimatedVehicleJourney> estimatedVehicleJourney) {
        this.estimatedVehicleJourney = estimatedVehicleJourney;
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
