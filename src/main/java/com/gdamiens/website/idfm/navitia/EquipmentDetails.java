package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "embedded_type",
    "current_availability"
})
@Generated("jsonschema2pojo")
public class EquipmentDetails {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("embedded_type")
    private EmbeddedEquipmentDetailsTypeEnum embeddedType;

//    @JsonProperty("current_availability")
//    private CurrentAvailability currentAvailability;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("embedded_type")
    public EmbeddedEquipmentDetailsTypeEnum getEmbeddedType() {
        return embeddedType;
    }

    @JsonProperty("embedded_type")
    public void setEmbeddedType(EmbeddedEquipmentDetailsTypeEnum embeddedType) {
        this.embeddedType = embeddedType;
    }

//    @JsonProperty("current_availability")
//    public CurrentAvailability getCurrentAvailability() {
//        return currentAvailability;
//    }
//
//    @JsonProperty("current_availability")
//    public void setCurrentAvailability(CurrentAvailability currentAvailability) {
//        this.currentAvailability = currentAvailability;
//    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
