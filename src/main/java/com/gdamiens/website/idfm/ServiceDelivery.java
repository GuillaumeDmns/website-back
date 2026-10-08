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
    "ProducerRef",
    "ResponseMessageIdentifier",
    "StopMonitoringDelivery",
    "EstimatedTimetableDelivery"
})
@Generated("jsonschema2pojo")
public class ServiceDelivery {

    @JsonProperty("ResponseTimestamp")
    private String responseTimestamp;
    @JsonProperty("ProducerRef")
    private String producerRef;
    @JsonProperty("ResponseMessageIdentifier")
    private String responseMessageIdentifier;
    @JsonProperty("StopMonitoringDelivery")
    private List<StopMonitoringDelivery> stopMonitoringDelivery = null;

    @JsonProperty("EstimatedTimetableDelivery")
    private List<EstimatedTimetableDelivery> estimatedTimetableDelivery = null;

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

    @JsonProperty("ProducerRef")
    public String getProducerRef() {
        return producerRef;
    }

    @JsonProperty("ProducerRef")
    public void setProducerRef(String producerRef) {
        this.producerRef = producerRef;
    }

    @JsonProperty("ResponseMessageIdentifier")
    public String getResponseMessageIdentifier() {
        return responseMessageIdentifier;
    }

    @JsonProperty("ResponseMessageIdentifier")
    public void setResponseMessageIdentifier(String responseMessageIdentifier) {
        this.responseMessageIdentifier = responseMessageIdentifier;
    }

    @JsonProperty("EstimatedTimetableDelivery")
    public List<EstimatedTimetableDelivery> getEstimatedTimetableDelivery() {
        return estimatedTimetableDelivery;
    }

    @JsonProperty("EstimatedTimetableDelivery")
    public void setEstimatedTimetableDelivery(List<EstimatedTimetableDelivery> estimatedTimetableDelivery) {
        this.estimatedTimetableDelivery = estimatedTimetableDelivery;
    }

    @JsonProperty("StopMonitoringDelivery")
    public List<StopMonitoringDelivery> getStopMonitoringDelivery() {
        return stopMonitoringDelivery;
    }

    @JsonProperty("StopMonitoringDelivery")
    public void setStopMonitoringDelivery(List<StopMonitoringDelivery> stopMonitoringDelivery) {
        this.stopMonitoringDelivery = stopMonitoringDelivery;
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
