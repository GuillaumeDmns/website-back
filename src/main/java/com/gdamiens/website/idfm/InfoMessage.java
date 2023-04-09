package com.gdamiens.website.idfm;

import java.util.LinkedHashMap;
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
    "FormatRef",
    "RecordedAtTime",
    "ItemIdentifier",
    "InfoMessageIdentifier",
    "InfoChannelRef",
    "ValidUntilTime",
    "Content"
})
@Generated("jsonschema2pojo")
public class InfoMessage {

    @JsonProperty("FormatRef")
    private String formatRef;
    @JsonProperty("RecordedAtTime")
    private String recordedAtTime;
    @JsonProperty("ItemIdentifier")
    private String itemIdentifier;
    @JsonProperty("InfoMessageIdentifier")
    private InfoMessageIdentifier infoMessageIdentifier;
    @JsonProperty("InfoChannelRef")
    private InfoChannelRef infoChannelRef;
    @JsonProperty("ValidUntilTime")
    private String validUntilTime;
    @JsonProperty("Content")
    private Content content;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("FormatRef")
    public String getFormatRef() {
        return formatRef;
    }

    @JsonProperty("FormatRef")
    public void setFormatRef(String formatRef) {
        this.formatRef = formatRef;
    }

    @JsonProperty("RecordedAtTime")
    public String getRecordedAtTime() {
        return recordedAtTime;
    }

    @JsonProperty("RecordedAtTime")
    public void setRecordedAtTime(String recordedAtTime) {
        this.recordedAtTime = recordedAtTime;
    }

    @JsonProperty("ItemIdentifier")
    public String getItemIdentifier() {
        return itemIdentifier;
    }

    @JsonProperty("ItemIdentifier")
    public void setItemIdentifier(String itemIdentifier) {
        this.itemIdentifier = itemIdentifier;
    }

    @JsonProperty("InfoMessageIdentifier")
    public InfoMessageIdentifier getInfoMessageIdentifier() {
        return infoMessageIdentifier;
    }

    @JsonProperty("InfoMessageIdentifier")
    public void setInfoMessageIdentifier(InfoMessageIdentifier infoMessageIdentifier) {
        this.infoMessageIdentifier = infoMessageIdentifier;
    }

    @JsonProperty("InfoChannelRef")
    public InfoChannelRef getInfoChannelRef() {
        return infoChannelRef;
    }

    @JsonProperty("InfoChannelRef")
    public void setInfoChannelRef(InfoChannelRef infoChannelRef) {
        this.infoChannelRef = infoChannelRef;
    }

    @JsonProperty("ValidUntilTime")
    public String getValidUntilTime() {
        return validUntilTime;
    }

    @JsonProperty("ValidUntilTime")
    public void setValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
    }

    @JsonProperty("Content")
    public Content getContent() {
        return content;
    }

    @JsonProperty("Content")
    public void setContent(Content content) {
        this.content = content;
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
