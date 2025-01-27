package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "beginning_date",
    "days"
})
@Generated("jsonschema2pojo")
public class ValidityPattern {

    private String beginningDate;

    @JsonProperty("days")
    private String days;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("beginningDate")
    public String getBeginningDate() {
        return beginningDate;
    }

    @JsonProperty("beginning_date")
    public void setBeginningDate(String beginningDate) {
        this.beginningDate = beginningDate;
    }

    @JsonProperty("days")
    public String getDays() {
        return days;
    }

    @JsonProperty("days")
    public void setDays(String days) {
        this.days = days;
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
