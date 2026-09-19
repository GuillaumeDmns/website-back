package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class EquipmentReport {

    @JsonProperty("line")
    private Line line;

    private List<StopAreaEquipments> stopAreaEquipments;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("line")
    public Line getLine() {
        return line;
    }

    @JsonProperty("line")
    public void setLine(Line line) {
        this.line = line;
    }

    @JsonProperty("stopAreaEquipments")
    public List<StopAreaEquipments> getStopAreaEquipments() {
        return stopAreaEquipments;
    }

    @JsonProperty("stop_area_equipments")
    public void setStopAreaEquipments(List<StopAreaEquipments> stopAreaEquipments) {
        this.stopAreaEquipments = stopAreaEquipments;
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