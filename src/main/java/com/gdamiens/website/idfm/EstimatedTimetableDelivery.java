
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
    "ResponseTimestamp",
    "Version",
    "Status",
    "EstimatedJourneyVersionFrame"
})
@Generated("jsonschema2pojo")
public class EstimatedTimetableDelivery {

    @JsonProperty("ResponseTimestamp")
    private String responseTimestamp;
    @JsonProperty("Version")
    private String version;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("EstimatedJourneyVersionFrame")
    private List<EstimatedJourneyVersionFrame> estimatedJourneyVersionFrame = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ResponseTimestamp")
    public String getResponseTimestamp() {
        return responseTimestamp;
    }

    @JsonProperty("ResponseTimestamp")
    public void setResponseTimestamp(String responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    @JsonProperty("Version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("Version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("Status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("Status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("EstimatedJourneyVersionFrame")
    public List<EstimatedJourneyVersionFrame> getEstimatedJourneyVersionFrame() {
        return estimatedJourneyVersionFrame;
    }

    @JsonProperty("EstimatedJourneyVersionFrame")
    public void setEstimatedJourneyVersionFrame(List<EstimatedJourneyVersionFrame> estimatedJourneyVersionFrame) {
        this.estimatedJourneyVersionFrame = estimatedJourneyVersionFrame;
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
