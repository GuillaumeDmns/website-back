package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "title",
    "rel",
    "templated",
    "internal",
    "type",
    "href",
    "value",
    "category",
    "comment_type"
})
@Generated("jsonschema2pojo")
public class LinkSchema {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("rel")
    private String rel;

    @JsonProperty("templated")
    private Boolean templated;

    @JsonProperty("internal")
    private Boolean internal;

    @JsonProperty("type")
    private String type;

    @JsonProperty("href")
    private String href;

    @JsonProperty("value")
    private String value;

    @JsonProperty("category")
    private String category;

    private String commentType;

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

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("rel")
    public String getRel() {
        return rel;
    }

    @JsonProperty("rel")
    public void setRel(String rel) {
        this.rel = rel;
    }

    @JsonProperty("templated")
    public Boolean getTemplated() {
        return templated;
    }

    @JsonProperty("templated")
    public void setTemplated(Boolean templated) {
        this.templated = templated;
    }

    @JsonProperty("internal")
    public Boolean getInternal() {
        return internal;
    }

    @JsonProperty("internal")
    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("href")
    public String getHref() {
        return href;
    }

    @JsonProperty("href")
    public void setHref(String href) {
        this.href = href;
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
    public String getCategory() {
        return category;
    }

    @JsonProperty("category")
    public void setCategory(String category) {
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
