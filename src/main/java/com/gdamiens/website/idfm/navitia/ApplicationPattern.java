package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "week_pattern",
    "application_period",
    "time_slots"
})
@Generated("jsonschema2pojo")
public class ApplicationPattern {

    private WeekPattern weekPattern;

    private PeriodDate applicationPeriod;

    private List<PeriodTime> timeSlots = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("weekPattern")
    public WeekPattern getWeekPattern() {
        return weekPattern;
    }

    @JsonProperty("week_pattern")
    public void setWeekPattern(WeekPattern weekPattern) {
        this.weekPattern = weekPattern;
    }

    @JsonProperty("applicationPeriod")
    public PeriodDate getApplicationPeriod() {
        return applicationPeriod;
    }

    @JsonProperty("application_period")
    public void setApplicationPeriod(PeriodDate applicationPeriod) {
        this.applicationPeriod = applicationPeriod;
    }

    @JsonProperty("timeSlots")
    public List<PeriodTime> getTimeSlots() {
        return timeSlots;
    }

    @JsonProperty("time_slots")
    public void setTimeSlots(List<PeriodTime> timeSlots) {
        this.timeSlots = timeSlots;
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
