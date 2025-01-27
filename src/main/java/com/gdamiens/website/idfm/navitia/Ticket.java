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
    "comment",
    "name",
    "links",
    "cost",
    "found",
    "id"
})
@Generated("jsonschema2pojo")
public class Ticket {

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("name")
    private String name;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    @JsonProperty("cost")
    private Cost cost;

    @JsonProperty("found")
    private Boolean found;

    @JsonProperty("id")
    private String id;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
    }

    @JsonProperty("cost")
    public Cost getCost() {
        return cost;
    }

    @JsonProperty("cost")
    public void setCost(Cost cost) {
        this.cost = cost;
    }

    @JsonProperty("found")
    public Boolean getFound() {
        return found;
    }

    @JsonProperty("found")
    public void setFound(Boolean found) {
        this.found = found;
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
