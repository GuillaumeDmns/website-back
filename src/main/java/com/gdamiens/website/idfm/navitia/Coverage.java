package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class Coverage {

    @JsonProperty("status")
    private String status;

    private String datasetCreatedAt;

    @JsonProperty("name")
    private String name;

    private String startProductionDate;

    @JsonProperty("shape")
    private Object shape;

    private String endProductionDate;

    @JsonProperty("error")
    private Error error;

    private String lastLoadAt;

    @JsonProperty("id")
    private String id;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("datasetCreatedAt")
    public String getDatasetCreatedAt() {
        return datasetCreatedAt;
    }

    @JsonProperty("dataset_created_at")
    public void setDatasetCreatedAt(String datasetCreatedAt) {
        this.datasetCreatedAt = datasetCreatedAt;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("startProductionDate")
    public String getStartProductionDate() {
        return startProductionDate;
    }

    @JsonProperty("start_production_date")
    public void setStartProductionDate(String startProductionDate) {
        this.startProductionDate = startProductionDate;
    }

    @JsonProperty("shape")
    public Object getShape() {
        return shape;
    }

    @JsonProperty("shape")
    public void setShape(Object shape) {
        this.shape = shape;
    }

    @JsonProperty("endProductionDate")
    public String getEndProductionDate() {
        return endProductionDate;
    }

    @JsonProperty("end_production_date")
    public void setEndProductionDate(String endProductionDate) {
        this.endProductionDate = endProductionDate;
    }

    @JsonProperty("error")
    public Error getError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(Error error) {
        this.error = error;
    }

    @JsonProperty("lastLoadAt")
    public String getLastLoadAt() {
        return lastLoadAt;
    }

    @JsonProperty("last_load_at")
    public void setLastLoadAt(String lastLoadAt) {
        this.lastLoadAt = lastLoadAt;
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