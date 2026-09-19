package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "current_datetime",
    "timezone"
})
@Generated("jsonschema2pojo")
public class Context {

    private String currentDatetime;

    @JsonProperty("timezone")
    private String timezone;

    private Object carDirectPath;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("currentDatetime")
    public String getCurrentDatetime() {
        return currentDatetime;
    }

    @JsonProperty("current_datetime")
    public void setCurrentDatetime(String currentDatetime) {
        this.currentDatetime = currentDatetime;
    }

    @JsonProperty("timezone")
    public String getTimezone() {
        return timezone;
    }

    @JsonProperty("timezone")
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @JsonProperty("carDirectPath")
    public Object getCarDirectPath() {
        return carDirectPath;
    }

    @JsonProperty("car_direct_path")
    public void setCarDirectPath(Object carDirectPath) {
        this.carDirectPath = carDirectPath;
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
