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
    "stop_point",
    "links",
    "arrival_date_time",
    "additional_informations",
    "departure_date_time",
    "base_arrival_date_time",
    "base_departure_date_time",
    "data_freshness"
})
@Generated("jsonschema2pojo")
public class StopDateTime {

    private StopPoint stopPoint;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    private String arrivalDateTime;

    private List<StopDateTimeAdditionalInformationsEnum> additionalInformations = null;

    private String departureDateTime;

    private String baseArrivalDateTime;

    private String baseDepartureDateTime;

    private DataFreshnessEnum dataFreshness;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("stopPoint")
    public StopPoint getStopPoint() {
        return stopPoint;
    }

    @JsonProperty("stop_point")
    public void setStopPoint(StopPoint stopPoint) {
        this.stopPoint = stopPoint;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
    }

    @JsonProperty("arrivalDateTime")
    public String getArrivalDateTime() {
        return arrivalDateTime;
    }

    @JsonProperty("arrival_date_time")
    public void setArrivalDateTime(String arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    @JsonProperty("additionalInformations")
    public List<StopDateTimeAdditionalInformationsEnum> getAdditionalInformations() {
        return additionalInformations;
    }

    @JsonProperty("additional_informations")
    public void setAdditionalInformations(List<StopDateTimeAdditionalInformationsEnum> additionalInformations) {
        this.additionalInformations = additionalInformations;
    }

    @JsonProperty("departureDateTime")
    public String getDepartureDateTime() {
        return departureDateTime;
    }

    @JsonProperty("departure_date_time")
    public void setDepartureDateTime(String departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    @JsonProperty("baseArrivalDateTime")
    public String getBaseArrivalDateTime() {
        return baseArrivalDateTime;
    }

    @JsonProperty("base_arrival_date_time")
    public void setBaseArrivalDateTime(String baseArrivalDateTime) {
        this.baseArrivalDateTime = baseArrivalDateTime;
    }

    @JsonProperty("baseDepartureDateTime")
    public String getBaseDepartureDateTime() {
        return baseDepartureDateTime;
    }

    @JsonProperty("base_departure_date_time")
    public void setBaseDepartureDateTime(String baseDepartureDateTime) {
        this.baseDepartureDateTime = baseDepartureDateTime;
    }

    @JsonProperty("dataFreshness")
    public DataFreshnessEnum getDataFreshness() {
        return dataFreshness;
    }

    @JsonProperty("data_freshness")
    public void setDataFreshness(DataFreshnessEnum dataFreshness) {
        this.dataFreshness = dataFreshness;
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
