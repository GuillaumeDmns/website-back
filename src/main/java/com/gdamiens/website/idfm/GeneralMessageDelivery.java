package com.gdamiens.website.idfm;

import java.util.LinkedHashMap;
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
    "InfoMessage"
})
@Generated("jsonschema2pojo")
public class GeneralMessageDelivery {

    @JsonProperty("ResponseTimestamp")
    private String responseTimestamp;
    @JsonProperty("Version")
    private String version;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("InfoMessage")
    private List<InfoMessage> infoMessage;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

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

    @JsonProperty("InfoMessage")
    public List<InfoMessage> getInfoMessage() {
        return infoMessage;
    }

    @JsonProperty("InfoMessage")
    public void setInfoMessage(List<InfoMessage> infoMessage) {
        this.infoMessage = infoMessage;
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
