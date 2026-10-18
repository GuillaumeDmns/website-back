package com.gdamiens.website.client.siri;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** Calls a vehicle journey already made (IDFM's new real-time platform, end of October 2026) */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "RecordedCall"
})
public class RecordedCalls {

    @JsonProperty("RecordedCall")
    private List<RecordedCall> recordedCall = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("RecordedCall")
    public List<RecordedCall> getRecordedCall() {
        return recordedCall;
    }

    @JsonProperty("RecordedCall")
    public void setRecordedCall(List<RecordedCall> recordedCall) {
        this.recordedCall = recordedCall;
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
