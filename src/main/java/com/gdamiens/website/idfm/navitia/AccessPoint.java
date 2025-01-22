package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "coord",
    "access_point_code",
    "embedded_type"
})
@Generated("jsonschema2pojo")
public class AccessPoint {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("coord")
    private Coord coord;

    private String accessPointCode;

    private EmbeddedAccessPointTypeEnum embeddedType;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("coord")
    public Coord getCoord() {
        return coord;
    }

    @JsonProperty("coord")
    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    @JsonProperty("getAccessPointCode")
    public String getAccessPointCode() {
        return accessPointCode;
    }

    @JsonProperty("access_point_code")
    public void setAccessPointCode(String accessPointCode) {
        this.accessPointCode = accessPointCode;
    }

    @JsonProperty("embeddedType")
    public EmbeddedAccessPointTypeEnum getEmbeddedType() {
        return embeddedType;
    }

    @JsonProperty("embedded_type")
    public void setEmbeddedType(EmbeddedAccessPointTypeEnum embeddedType) {
        this.embeddedType = embeddedType;
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
