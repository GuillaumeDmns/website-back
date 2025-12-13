package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "error",
    "feed_publishers",
    "links",
    "journeys",
    "tickets",
    "disruptions",
    "terminus",
    "context",
    "notes",
    "exceptions",
})
@Generated("jsonschema2pojo")
public class Journeys {

    @JsonProperty("error")
    private Error error;

    private List<FeedPublisher> feedPublishers = null;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    @JsonProperty("journeys")
    private List<Journey> journeys = null;

    @JsonProperty("tickets")
    private List<Ticket> tickets = null;

    @JsonProperty("disruptions")
    private List<Disruption> disruptions = null;

    @JsonProperty("terminus")
    private List<StopArea> terminus = null;

    @JsonProperty("context")
    private Context context;

    @JsonProperty("notes")
    private List<Note> notes = null;

    @JsonProperty("exceptions")
    private List<JourneyExc> exceptions = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("error")
    public Error getError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(Error error) {
        this.error = error;
    }

    @JsonProperty("feedPublishers")
    public List<FeedPublisher> getFeedPublishers() {
        return feedPublishers;
    }

    @JsonProperty("feed_publishers")
    public void setFeedPublishers(List<FeedPublisher> feedPublishers) {
        this.feedPublishers = feedPublishers;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
    }

    @JsonProperty("journeys")
    public List<Journey> getJourneys() {
        return journeys;
    }

    @JsonProperty("journeys")
    public void setJourneys(List<Journey> journeys) {
        this.journeys = journeys;
    }

    @JsonProperty("tickets")
    public List<Ticket> getTickets() {
        return tickets;
    }

    @JsonProperty("tickets")
    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @JsonProperty("disruptions")
    public List<Disruption> getDisruptions() {
        return disruptions;
    }

    @JsonProperty("disruptions")
    public void setDisruptions(List<Disruption> disruptions) {
        this.disruptions = disruptions;
    }

    @JsonProperty("terminus")
    public List<StopArea> getTerminus() {
        return terminus;
    }

    @JsonProperty("terminus")
    public void setTerminus(List<StopArea> terminus) {
        this.terminus = terminus;
    }

    @JsonProperty("context")
    public Context getContext() {
        return context;
    }

    @JsonProperty("context")
    public void setContext(Context context) {
        this.context = context;
    }

    @JsonProperty("notes")
    public List<Note> getNotes() {
        return notes;
    }

    @JsonProperty("notes")
    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @JsonProperty("exceptions")
    public List<JourneyExc> getExceptions() {
        return exceptions;
    }

    @JsonProperty("exceptions")
    public void setExceptions(List<JourneyExc> exceptions) {
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
