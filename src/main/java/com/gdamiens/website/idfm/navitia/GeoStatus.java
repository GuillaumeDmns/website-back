package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class GeoStatus {

    private Integer nbAdminsFromCities;

    private List<String> streetNetworkSources;

    private List<String> poiSources;

    private Integer nbAddresses;

    private Integer nbAdmins;

    private Integer nbPois;

    private Integer nbWays;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("nbAdminsFromCities")
    public Integer getNbAdminsFromCities() {
        return nbAdminsFromCities;
    }

    @JsonProperty("nb_admins_from_cities")
    public void setNbAdminsFromCities(Integer nbAdminsFromCities) {
        this.nbAdminsFromCities = nbAdminsFromCities;
    }

    @JsonProperty("streetNetworkSources")
    public List<String> getStreetNetworkSources() {
        return streetNetworkSources;
    }

    @JsonProperty("street_network_sources")
    public void setStreetNetworkSources(List<String> streetNetworkSources) {
        this.streetNetworkSources = streetNetworkSources;
    }

    @JsonProperty("poiSources")
    public List<String> getPoiSources() {
        return poiSources;
    }

    @JsonProperty("poi_sources")
    public void setPoiSources(List<String> poiSources) {
        this.poiSources = poiSources;
    }

    @JsonProperty("nbAddresses")
    public Integer getNbAddresses() {
        return nbAddresses;
    }

    @JsonProperty("nb_addresses")
    public void setNbAddresses(Integer nbAddresses) {
        this.nbAddresses = nbAddresses;
    }

    @JsonProperty("nbAdmins")
    public Integer getNbAdmins() {
        return nbAdmins;
    }

    @JsonProperty("nb_admins")
    public void setNbAdmins(Integer nbAdmins) {
        this.nbAdmins = nbAdmins;
    }

    @JsonProperty("nbPois")
    public Integer getNbPois() {
        return nbPois;
    }

    @JsonProperty("nb_pois")
    public void setNbPois(Integer nbPois) {
        this.nbPois = nbPois;
    }

    @JsonProperty("nbWays")
    public Integer getNbWays() {
        return nbWays;
    }

    @JsonProperty("nb_ways")
    public void setNbWays(Integer nbWays) {
        this.nbWays = nbWays;
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