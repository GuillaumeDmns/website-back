package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class DateTimeType {

    private String dateTime;

    private List<String> additionalInformations;

    private String baseDateTime;

    private String dataFreshness;

    @JsonProperty("links")
    private List<LinkSchema> links;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("dateTime")
    public String getDateTime() {
        return dateTime;
    }

    @JsonProperty("date_time")
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @JsonProperty("additionalInformations")
    public List<String> getAdditionalInformations() {
        return additionalInformations;
    }

    @JsonProperty("additional_informations")
    public void setAdditionalInformations(List<String> additionalInformations) {
        this.additionalInformations = additionalInformations;
    }

    @JsonProperty("baseDateTime")
    public String getBaseDateTime() {
        return baseDateTime;
    }

    @JsonProperty("base_date_time")
    public void setBaseDateTime(String baseDateTime) {
        this.baseDateTime = baseDateTime;
    }

    @JsonProperty("dataFreshness")
    public String getDataFreshness() {
        return dataFreshness;
    }

    @JsonProperty("data_freshness")
    public void setDataFreshness(String dataFreshness) {
        this.dataFreshness = dataFreshness;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
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