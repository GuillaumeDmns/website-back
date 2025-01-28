package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "direction",
    "code",
    "network",
    "links",
    "color",
    "name",
    "physical_mode",
    "headsign",
    "label",
    "equipments",
    "text_color",
    "headsigns",
    "commercial_mode",
    "description"
})
@Generated("jsonschema2pojo")
public class VJDisplayInformation {

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("code")
    private String code;

    @JsonProperty("network")
    private String network;

    @JsonProperty("links")
    private List<LinkSchema> links;

    @JsonProperty("color")
    private String color;

    @JsonProperty("name")
    private String name;

    private String physicalMode;

    @JsonProperty("headsign")
    private String headsign;

    @JsonProperty("label")
    private String label;

    @JsonProperty("equipments")
    private List<EquipmentsEnum> equipments = null;

    private String textColor;

    @JsonProperty("headsigns")
    private List<String> headsigns = null;

    private String commercialMode;

    @JsonProperty("description")
    private String description;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("direction")
    public String getDirection() {
        return direction;
    }

    @JsonProperty("direction")
    public void setDirection(String direction) {
        this.direction = direction;
    }

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("network")
    public String getNetwork() {
        return network;
    }

    @JsonProperty("network")
    public void setNetwork(String network) {
        this.network = network;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
    }

    @JsonProperty("color")
    public String getColor() {
        return color;
    }

    @JsonProperty("color")
    public void setColor(String color) {
        this.color = color;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("physicalMode")
    public String getPhysicalMode() {
        return physicalMode;
    }

    @JsonProperty("physical_mode")
    public void setPhysicalMode(String physicalMode) {
        this.physicalMode = physicalMode;
    }

    @JsonProperty("headsign")
    public String getHeadsign() {
        return headsign;
    }

    @JsonProperty("headsign")
    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("equipments")
    public List<EquipmentsEnum> getEquipments() {
        return equipments;
    }

    @JsonProperty("equipments")
    public void setEquipments(List<EquipmentsEnum> equipments) {
        this.equipments = equipments;
    }

    @JsonProperty("textColor")
    public String getTextColor() {
        return textColor;
    }

    @JsonProperty("text_color")
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    @JsonProperty("headsigns")
    public List<String> getHeadsigns() {
        return headsigns;
    }

    @JsonProperty("headsigns")
    public void setHeadsigns(List<String> headsigns) {
        this.headsigns = headsigns;
    }

    @JsonProperty("commercialMode")
    public String getCommercialMode() {
        return commercialMode;
    }

    @JsonProperty("commercial_mode")
    public void setCommercialMode(String commercialMode) {
        this.commercialMode = commercialMode;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
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
