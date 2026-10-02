package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "total_result",
    "start_page",
    "items_per_page",
    "items_on_page"
})
@Generated("jsonschema2pojo")
public class Pagination {
    private int totalResult;

    private int startPage;

    private int itemsPerPage;

    private int itemsOnPage;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("totalResult")
    public int getTotalResult() {
        return totalResult;
    }

    @JsonProperty("total_result")
    public void setTotalResult(int totalResult) {
        this.totalResult = totalResult;
    }

    @JsonProperty("startPage")
    public int getStartPage() {
        return startPage;
    }

    @JsonProperty("start_page")
    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    @JsonProperty("itemsPerPage")
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    @JsonProperty("items_per_page")
    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    @JsonProperty("itemsOnPage")
    public int getItemsOnPage() {
        return itemsOnPage;
    }

    @JsonProperty("items_on_page")
    public void setItemsOnPage(int itemsOnPage) {
        this.itemsOnPage = itemsOnPage;
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
