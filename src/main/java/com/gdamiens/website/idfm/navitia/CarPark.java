package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "available",
    "occupied",
    "available_PRM",
    "occupied_PRM",
    "total_places",
    "available_ridesharing",
    "occupied_ridesharing",
    "available_electric_vehicle",
    "occupied_electric_vehicle",
    "state",
    "availability"
})
@Generated("jsonschema2pojo")
public class CarPark {

    @JsonProperty("available")
    private Integer available;

    @JsonProperty("occupied")
    private Integer occupied;

    private Integer availablePRM;

    private Integer occupiedPRM;

    private Integer totalPlaces;

    private Integer availableRidesharing;

    private Integer occupiedRidesharing;

    private Integer availableElectricVehicle;

    private Integer occupiedElectricVehicle;

    @JsonProperty("state")
    private String state;

    @JsonProperty("availability")
    private Boolean availability;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("available")
    public Integer getAvailable() {
        return available;
    }

    @JsonProperty("available")
    public void setAvailable(Integer available) {
        this.available = available;
    }

    @JsonProperty("occupied")
    public Integer getOccupied() {
        return occupied;
    }

    @JsonProperty("occupied")
    public void setOccupied(Integer occupied) {
        this.occupied = occupied;
    }

    @JsonProperty("availablePRM")
    public Integer getAvailablePRM() {
        return availablePRM;
    }

    @JsonProperty("available_PRM")
    public void setAvailablePRM(Integer availablePRM) {
        this.availablePRM = availablePRM;
    }

    @JsonProperty("occupiedPRM")
    public Integer getOccupiedPRM() {
        return occupiedPRM;
    }

    @JsonProperty("occupied_PRM")
    public void setOccupiedPRM(Integer occupiedPRM) {
        this.occupiedPRM = occupiedPRM;
    }

    @JsonProperty("totalPlaces")
    public Integer getTotalPlaces() {
        return totalPlaces;
    }

    @JsonProperty("total_places")
    public void setTotalPlaces(Integer totalPlaces) {
        this.totalPlaces = totalPlaces;
    }

    @JsonProperty("availableRidesharing")
    public Integer getAvailableRidesharing() {
        return availableRidesharing;
    }

    @JsonProperty("available_ridesharing")
    public void setAvailableRidesharing(Integer availableRidesharing) {
        this.availableRidesharing = availableRidesharing;
    }

    @JsonProperty("occupiedRidesharing")
    public Integer getOccupiedRidesharing() {
        return occupiedRidesharing;
    }

    @JsonProperty("occupied_ridesharing")
    public void setOccupiedRidesharing(Integer occupiedRidesharing) {
        this.occupiedRidesharing = occupiedRidesharing;
    }

    @JsonProperty("availableElectricVehicle")
    public Integer getAvailableElectricVehicle() {
        return availableElectricVehicle;
    }

    @JsonProperty("available_electric_vehicle")
    public void setAvailableElectricVehicle(Integer availableElectricVehicle) {
        this.availableElectricVehicle = availableElectricVehicle;
    }

    @JsonProperty("occupiedElectricVehicle")
    public Integer getOccupiedElectricVehicle() {
        return occupiedElectricVehicle;
    }

    @JsonProperty("occupied_electric_vehicle")
    public void setOccupiedElectricVehicle(Integer occupiedElectricVehicle) {
        this.occupiedElectricVehicle = occupiedElectricVehicle;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("availability")
    public Boolean getAvailability() {
        return availability;
    }

    @JsonProperty("availability")
    public void setAvailability(Boolean availability) {
        this.availability = availability;
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
