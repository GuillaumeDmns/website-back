package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "house_number",
    "coord",
    "label",
    "administrative_regions"
})
@Generated("jsonschema2pojo")
public class Address {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    private Integer houseNumber;

    @JsonProperty("coord")
    private Coord coord;

    @JsonProperty("label")
    private String label;

    private List<Admin> administrativeRegions = null;

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

    @JsonProperty("houseNumber")
    public Integer getHouseNumber() {
        return houseNumber;
    }

    @JsonProperty("house_number")
    public void setHouseNumber(Integer houseNumber) {
        this.houseNumber = houseNumber;
    }

    @JsonProperty("coord")
    public Coord getCoord() {
        return coord;
    }

    @JsonProperty("coord")
    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("administrativeRegions")
    public List<Admin> getAdministrativeRegions() {
        return administrativeRegions;
    }

    @JsonProperty("administrative_regions")
    public void setAdministrativeRegions(List<Admin> administrativeRegions) {
        this.administrativeRegions = administrativeRegions;
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
