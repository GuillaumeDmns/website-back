package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class RidesharingInformation {

    @JsonProperty("operator")
    private Companie operator;

    @JsonProperty("driver")
    private IndividualInformation driver;

    @JsonProperty("network")
    private Network network;

    @JsonProperty("seats")
    private SeatsDescription seats;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("operator")
    public Companie getOperator() {
        return operator;
    }

    @JsonProperty("operator")
    public void setOperator(Companie operator) {
        this.operator = operator;
    }

    @JsonProperty("driver")
    public IndividualInformation getDriver() {
        return driver;
    }

    @JsonProperty("driver")
    public void setDriver(IndividualInformation driver) {
        this.driver = driver;
    }

    @JsonProperty("network")
    public Network getNetwork() {
        return network;
    }

    @JsonProperty("network")
    public void setNetwork(Network network) {
        this.network = network;
    }

    @JsonProperty("seats")
    public SeatsDescription getSeats() {
        return seats;
    }

    @JsonProperty("seats")
    public void setSeats(SeatsDescription seats) {
        this.seats = seats;
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