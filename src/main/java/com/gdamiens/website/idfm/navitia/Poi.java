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
    "coord",
    "links",
    "label",
    "administrative_regions",
    "poi_type",
    "properties",
    "address",
    "stands",
    "car_park"
})
@Generated("jsonschema2pojo")
public class Poi {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("coord")
    private Coord coord;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    @JsonProperty("label")
    private String label;

    private List<Admin> administrativeRegions = null;

    private PoiType poiType;

    @JsonProperty("properties")
    private Map<String, String> properties;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("stands")
    private Stands stands;

    private CarPark carPark;

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

    @JsonProperty("coord")
    public Coord getCoord() {
        return coord;
    }

    @JsonProperty("coord")
    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
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

    @JsonProperty("poiType")
    public PoiType getPoiType() {
        return poiType;
    }

    @JsonProperty("poi_type")
    public void setPoiType(PoiType poiType) {
        this.poiType = poiType;
    }

    @JsonProperty("properties")
    public Map<String, String> getProperties() {
        return properties;
    }

    @JsonProperty("properties")
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @JsonProperty("address")
    public Address getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(Address address) {
        this.address = address;
    }

    @JsonProperty("stands")
    public Stands getStands() {
        return stands;
    }

    @JsonProperty("stands")
    public void setStands(Stands stands) {
        this.stands = stands;
    }

    @JsonProperty("carPark")
    public CarPark getCarPark() {
        return carPark;
    }

    @JsonProperty("car_park")
    public void setCarPark(CarPark carPark) {
        this.carPark = carPark;
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
