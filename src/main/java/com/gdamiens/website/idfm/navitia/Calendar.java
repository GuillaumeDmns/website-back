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
    "active_periods",
    "name",
    "validity_pattern",
    "exceptions",
    "week_pattern",
    "id"
})
@Generated("jsonschema2pojo")
public class Calendar {

    private List<CalendarPeriod> activePeriods = null;

    @JsonProperty("name")
    private String name;

    private ValidityPattern validityPattern;

    @JsonProperty("exceptions")
    private List<CalendarExc> exceptions = null;

    private WeekPattern weekPattern ;

    @JsonProperty("id")
    private String id;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("activePeriods")
    public List<CalendarPeriod> getActivePeriods() {
        return activePeriods;
    }

    @JsonProperty("active_periods")
    public void setActivePeriods(List<CalendarPeriod> activePeriods) {
        this.activePeriods = activePeriods;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("validityPattern")
    public ValidityPattern getValidityPattern() {
        return validityPattern;
    }

    @JsonProperty("validity_pattern")
    public void setValidityPattern(ValidityPattern validityPattern) {
        this.validityPattern = validityPattern;
    }

    @JsonProperty("exceptions")
    public List<CalendarExc> getExceptions() {
        return exceptions;
    }

    @JsonProperty("exceptions")
    public void setExceptions(List<CalendarExc> exceptions) {
        this.exceptions = exceptions;
    }

    @JsonProperty("weekPattern")
    public WeekPattern getWeekPattern() {
        return weekPattern;
    }

    @JsonProperty("week_pattern")
    public void setWeekPattern(WeekPattern weekPattern) {
        this.weekPattern = weekPattern;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
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
