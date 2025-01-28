package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "disruption_id",
    "impact_id",
    "application_periods",
    "application_patterns",
    "status",
    "updated_at",
    "tags",
    "cause",
    "category",
    "severity",
    "messages",
    "impacted_objects",
    "uri",
    "disruption_uri",
    "contributor",
    "properties",
    "notes",
    "exceptions"
})
@Generated("jsonschema2pojo")
public class Disruption {

    @JsonProperty("id")
    private String id;

    private String disruptionId;

    private String impactId;

    private List<Period> applicationPeriods = null;

    private List<ApplicationPattern> applicationPatterns = null;

    @JsonProperty("status")
    private DisruptionStatusEnum status;

    private String updatedAt;

    @JsonProperty("tags")
    private List<String> tags = null;

    @JsonProperty("cause")
    private String cause;

    @JsonProperty("category")
    private String category = null;

    @JsonProperty("severity")
    private Severity severity;

    @JsonProperty("messages")
    private List<Message> messages = null;

    private List<Impacted> impactedObjects = null;

    @JsonProperty("uri")
    private String uri;

    private String disruptionUri;

    @JsonProperty("contributor")
    private String contributor;

    @JsonProperty("notes")
    private List<DisruptionProperty> properties = null;

    @JsonProperty("exceptions")
    private List<Exception> exceptions = null;

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

    @JsonProperty("disruptionId")
    public String getDisruptionId() {
        return disruptionId;
    }

    @JsonProperty("disruption_id")
    public void setDisruptionId(String disruptionId) {
        this.disruptionId = disruptionId;
    }

    @JsonProperty("impactId")
    public String getImpactId() {
        return impactId;
    }

    @JsonProperty("impact_id")
    public void setImpactId(String impactId) {
        this.impactId = impactId;
    }

    @JsonProperty("applicationPeriods")
    public List<Period> getApplicationPeriods() {
        return applicationPeriods;
    }

    @JsonProperty("application_periods")
    public void setApplicationPeriods(List<Period> applicationPeriods) {
        this.applicationPeriods = applicationPeriods;
    }

    @JsonProperty("applicationPatterns")
    public List<ApplicationPattern> getApplicationPatterns() {
        return applicationPatterns;
    }

    @JsonProperty("application_patterns")
    public void setApplicationPatterns(List<ApplicationPattern> applicationPatterns) {
        this.applicationPatterns = applicationPatterns;
    }

    @JsonProperty("status")
    public DisruptionStatusEnum getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(DisruptionStatusEnum status) {
        this.status = status;
    }

    @JsonProperty("updatedAt")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("tags")
    public List<String> getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @JsonProperty("cause")
    public String getCause() {
        return cause;
    }

    @JsonProperty("cause")
    public void setCause(String cause) {
        this.cause = cause;
    }

    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    @JsonProperty("category")
    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("severity")
    public Severity getSeverity() {
        return severity;
    }

    @JsonProperty("severity")
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @JsonProperty("messages")
    public List<Message> getMessages() {
        return messages;
    }

    @JsonProperty("messages")
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @JsonProperty("impactedObjects")
    public List<Impacted> getImpactedObjects() {
        return impactedObjects;
    }

    @JsonProperty("impacted_objects")
    public void setImpactedObjects(List<Impacted> impactedObjects) {
        this.impactedObjects = impactedObjects;
    }

    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonProperty("disruptionUri")
    public String getDisruptionUri() {
        return disruptionUri;
    }

    @JsonProperty("disruption_uri")
    public void setDisruptionUri(String disruptionUri) {
        this.disruptionUri = disruptionUri;
    }

    @JsonProperty("contributor")
    public String getContributor() {
        return contributor;
    }

    @JsonProperty("contributor")
    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    @JsonProperty("properties")
    public List<DisruptionProperty> getProperties() {
        return properties;
    }

    @JsonProperty("properties")
    public void setProperties(List<DisruptionProperty> properties) {
        this.properties = properties;
    }

    @JsonProperty("exceptions")
    public List<Exception> getExceptions() {
        return exceptions;
    }

    @JsonProperty("exceptions")
    public void setExceptions(List<Exception> exceptions) {
        this.exceptions = exceptions;
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
