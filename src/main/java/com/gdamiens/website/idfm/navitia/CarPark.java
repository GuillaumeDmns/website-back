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

    @JsonProperty("available_PRM")
    private Integer availablePRM;

    @JsonProperty("occupied_PRM")
    private Integer occupiedPRM;

    @JsonProperty("total_places")
    private Integer totalPlaces;

    @JsonProperty("available_ridesharing")
    private Integer availableRidesharing;

    @JsonProperty("occupied_ridesharing")
    private Integer occupiedRidesharing;

    @JsonProperty("available_electric_vehicle")
    private Integer availableElectricVehicle;

    @JsonProperty("occupied_electric_vehicle")
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

    @JsonProperty("available_PRM")
    public Integer getAvailablePRM() {
        return availablePRM;
    }

    @JsonProperty("available_PRM")
    public void setAvailablePRM(Integer availablePRM) {
        this.availablePRM = availablePRM;
    }

    @JsonProperty("occupied_PRM")
    public Integer getOccupiedPRM() {
        return occupiedPRM;
    }

    @JsonProperty("occupied_PRM")
    public void setOccupiedPRM(Integer occupiedPRM) {
        this.occupiedPRM = occupiedPRM;
    }

    @JsonProperty("total_places")
    public Integer getTotalPlaces() {
        return totalPlaces;
    }

    @JsonProperty("total_places")
    public void setTotalPlaces(Integer totalPlaces) {
        this.totalPlaces = totalPlaces;
    }

    @JsonProperty("available_ridesharing")
    public Integer getAvailableRidesharing() {
        return availableRidesharing;
    }

    @JsonProperty("available_ridesharing")
    public void setAvailableRidesharing(Integer availableRidesharing) {
        this.availableRidesharing = availableRidesharing;
    }

    @JsonProperty("occupied_ridesharing")
    public Integer getOccupiedRidesharing() {
        return occupiedRidesharing;
    }

    @JsonProperty("occupied_ridesharing")
    public void setOccupiedRidesharing(Integer occupiedRidesharing) {
        this.occupiedRidesharing = occupiedRidesharing;
    }

    @JsonProperty("available_electric_vehicle")
    public Integer getAvailableElectricVehicle() {
        return availableElectricVehicle;
    }

    @JsonProperty("available_electric_vehicle")
    public void setAvailableElectricVehicle(Integer availableElectricVehicle) {
        this.availableElectricVehicle = availableElectricVehicle;
    }

    @JsonProperty("occupied_electric_vehicle")
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
