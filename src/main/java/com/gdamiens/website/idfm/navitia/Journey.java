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
    "status",
    "distances",
    "from",
    "tags",
    "nb_transfers",
    "durations",
    "arrival_date_time",
    "calendars",
    "departure_date_time",
    "to",
    "requested_date_time",
    "fare",
    "co2_emission",
    "type",
    "duration",
    "sections",
    "debug"
})
@Generated("jsonschema2pojo")
public class Journey {

    @JsonProperty("status")
    private String status;

    @JsonProperty("distances")
    private Distances distances;

    @JsonProperty("from")
    private Place from;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    @JsonProperty("tags")
    private List<String> tags = null;

    private Integer nbTransfers;

    @JsonProperty("durations")
    private Durations durations;

    private String arrivalDateTime;

    @JsonProperty("calendars")
    private List<Calendar> calendars = null;

    private String departureDateTime;

    @JsonProperty("to")
    private Place to;

    private String requestedDateTime;

    @JsonProperty("fare")
    private Fare fare;

    private Amount co2Emission;

    @JsonProperty("type")
    private String type;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("sections")
    private List<Section> sections = null;

    @JsonProperty("debug")
    private JourneyDebug debug;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("distances")
    public Distances getDistances() {
        return distances;
    }

    @JsonProperty("distances")
    public void setDistances(Distances distances) {
        this.distances = distances;
    }

    @JsonProperty("from")
    public Place getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(Place from) {
        this.from = from;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
    }

    @JsonProperty("tags")
    public List<String> getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @JsonProperty("nbTransfers")
    public Integer getNbTransfers() {
        return nbTransfers;
    }

    @JsonProperty("nb_transfers")

    public void setNbTransfers(Integer nbTransfers) {
        this.nbTransfers = nbTransfers;
    }

    @JsonProperty("durations")
    public Durations getDurations() {
        return durations;
    }

    @JsonProperty("durations")
    public void setDurations(Durations durations) {
        this.durations = durations;
    }

    @JsonProperty("arrivalDateTime")
    public String getArrivalDateTime() {
        return arrivalDateTime;
    }

    @JsonProperty("arrival_date_time")
    public void setArrivalDateTime(String arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    @JsonProperty("calendars")
    public List<Calendar> getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    public void setCalendars(List<Calendar> calendars) {
        this.calendars = calendars;
    }

    @JsonProperty("departureDateTime")
    public String getDepartureDateTime() {
        return departureDateTime;
    }

    @JsonProperty("departure_date_time")
    public void setDepartureDateTime(String departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    @JsonProperty("to")
    public Place getTo() {
        return to;
    }

    @JsonProperty("to")
    public void setTo(Place to) {
        this.to = to;
    }

    @JsonProperty("requestedDateTime")
    public String getRequestedDateTime() {
        return requestedDateTime;
    }

    @JsonProperty("requested_date_time")
    public void setRequestedDateTime(String requestedDateTime) {
        this.requestedDateTime = requestedDateTime;
    }

    @JsonProperty("fare")
    public Fare getFare() {
        return fare;
    }

    @JsonProperty("fare")
    public void setFare(Fare fare) {
        this.fare = fare;
    }

    @JsonProperty("co2Emission")
    public Amount getCo2Emission() {
        return co2Emission;
    }

    @JsonProperty("co2_emission")
    public void setCo2Emission(Amount co2Emission) {
        this.co2Emission = co2Emission;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("duration")
    public Integer getDuration() {
        return duration;
    }

    @JsonProperty("duration")
    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @JsonProperty("sections")
    public List<Section> getSections() {
        return sections;
    }

    @JsonProperty("sections")
    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    @JsonProperty("debug")
    public JourneyDebug getDebug() {
        return debug;
    }

    @JsonProperty("debug")
    public void setDebug(JourneyDebug debug) {
        this.debug = debug;
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
