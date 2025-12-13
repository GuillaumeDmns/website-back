package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "id",
    "value",
    "category",
    "commentType",
})
@Generated("jsonschema2pojo")
public class Note {

    @JsonProperty("type")
    private String type;

    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private String value;

    @JsonProperty("category")
    private CategoryEnum category;

    private String commentType;

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

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    @JsonProperty("category")
    public CategoryEnum getCategory() {
        return category;
    }

    @JsonProperty("category")
    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    @JsonProperty("commentType")
    public String getCommentType() {
        return commentType;
    }

    @JsonProperty("comment_type")
    public void setCommentType(String commentType) {
        this.commentType = commentType;
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
