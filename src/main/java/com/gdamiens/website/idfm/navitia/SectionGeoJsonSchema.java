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
    "type",
    "properties",
    "coordinates"
})
@Generated("jsonschema2pojo")
public class SectionGeoJsonSchema {

    @JsonProperty("type")
    private String type;

    @JsonProperty("properties")
    private List<SectionGeoJsonSchemaProperties> properties = null;

    @JsonProperty("coordinates")
    private List<List<Float>> coordinates = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("properties")
    public List<SectionGeoJsonSchemaProperties> getProperties() {
        return properties;
    }

    @JsonProperty("properties")
    public void setProperties(List<SectionGeoJsonSchemaProperties> properties) {
        this.properties = properties;
    }

    @JsonProperty("coordinates")
    public List<List<Float>> getCoordinates() {
        return coordinates;
    }

    @JsonProperty("coordinates")
    public void setCoordinates(List<List<Float>> coordinates) {
        this.coordinates = coordinates;
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
