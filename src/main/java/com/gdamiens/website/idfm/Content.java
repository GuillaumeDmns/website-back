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
    "LineRef",
    "Message"
})
@Generated("jsonschema2pojo")
public class Content {

    @JsonProperty("LineRef")
    private List<LineRef> lineRef;
    @JsonProperty("Message")
    private List<Message> message;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("LineRef")
    public List<LineRef> getLineRef() {
        return lineRef;
    }

    @JsonProperty("LineRef")
    public void setLineRef(List<LineRef> lineRef) {
        this.lineRef = lineRef;
    }

    @JsonProperty("Message")
    public List<Message> getMessage() {
        return message;
    }

    @JsonProperty("Message")
    public void setMessage(List<Message> message) {
        this.message = message;
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
