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
public class Section {

    @JsonProperty("status")
    private VJDisplayInformation displayInformations;

    @JsonProperty("from")
    private Place from;

    @JsonProperty("links")
    private List<LinkSchema> links = null;

    private SectionTransferTypeEnum transferType;

    private String arrivalDateTime = null;

    private List<SectionAdditionalInformationsEnum> additionalInformations = null;

    private String departureDateTime;

    private RidesharingInformation ridesharingInformations;

    @JsonProperty("to")
    private Place to;

    private String baseArrivalDateTime;

    private String baseDepartureDateTime;

    private Amount co2Emission;

    private List<Journey> ridesharingJourneys = null;

    @JsonProperty("geojson")
    private SectionGeoJsonSchema geojson;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("path")
    private List<Path> path = null;

    private List<StopDateTime> stopDateTimes = null;

    @JsonProperty("type")
    private SectionTypeEnum type;

    @JsonProperty("id")
    private String id;

    private SectionDataFreshnessEnum dataFreshness;

    @JsonProperty("mode")
    private SectionModeEnum mode;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("displayInformations")
    public VJDisplayInformation getDisplayInformations() {
        return displayInformations;
    }

    @JsonProperty("display_informations")
    public void setDisplayInformations(VJDisplayInformation displayInformations) {
        this.displayInformations = displayInformations;
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

    @JsonProperty("transferType")
    public SectionTransferTypeEnum getTransferType() {
        return transferType;
    }

    @JsonProperty("transfer_type")
    public void setTransferType(SectionTransferTypeEnum transferType) {
        this.transferType = transferType;
    }

    @JsonProperty("arrivalDateTime")
    public String getArrivalDateTime() {
        return arrivalDateTime;
    }

    @JsonProperty("arrival_date_time")
    public void setArrivalDateTime(String arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    @JsonProperty("additionalInformations")
    public List<SectionAdditionalInformationsEnum> getAdditionalInformations() {
        return additionalInformations;
    }

    @JsonProperty("additional_informations")
    public void setAdditionalInformations(List<SectionAdditionalInformationsEnum> additionalInformations) {
        this.additionalInformations = additionalInformations;
    }

    @JsonProperty("departureDateTime")
    public String getDepartureDateTime() {
        return departureDateTime;
    }

    @JsonProperty("departure_date_time")
    public void setDepartureDateTime(String departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    @JsonProperty("ridesharingInformations")
    public RidesharingInformation getRidesharingInformations() {
        return ridesharingInformations;
    }

    @JsonProperty("ridesharing_informations")
    public void setRidesharingInformations(RidesharingInformation ridesharingInformations) {
        this.ridesharingInformations = ridesharingInformations;
    }

    @JsonProperty("to")
    public Place getTo() {
        return to;
    }

    @JsonProperty("to")
    public void setTo(Place to) {
        this.to = to;
    }

    @JsonProperty("baseArrivalDateTime")
    public String getBaseArrivalDateTime() {
        return baseArrivalDateTime;
    }

    @JsonProperty("base_arrival_date_time")
    public void setBaseArrivalDateTime(String baseArrivalDateTime) {
        this.baseArrivalDateTime = baseArrivalDateTime;
    }

    @JsonProperty("baseDepartureDateTime")
    public String getBaseDepartureDateTime() {
        return baseDepartureDateTime;
    }

    @JsonProperty("base_departure_date_time")
    public void setBaseDepartureDateTime(String baseDepartureDateTime) {
        this.baseDepartureDateTime = baseDepartureDateTime;
    }

    @JsonProperty("co2Emission")
    public Amount getCo2Emission() {
        return co2Emission;
    }

    @JsonProperty("co2_emission")
    public void setCo2Emission(Amount co2Emission) {
        this.co2Emission = co2Emission;
    }

    @JsonProperty("ridesharingJourneys")
    public List<Journey> getRidesharingJourneys() {
        return ridesharingJourneys;
    }

    @JsonProperty("ridesharing_journeys")
    public void setRidesharingJourneys(List<Journey> ridesharingJourneys) {
        this.ridesharingJourneys = ridesharingJourneys;
    }

    @JsonProperty("geojson")
    public SectionGeoJsonSchema getGeojson() {
        return geojson;
    }

    @JsonProperty("geojson")
    public void setGeojson(SectionGeoJsonSchema geojson) {
        this.geojson = geojson;
    }

    @JsonProperty("duration")
    public Integer getDuration() {
        return duration;
    }

    @JsonProperty("duration")
    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @JsonProperty("path")
    public List<Path> getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(List<Path> path) {
        this.path = path;
    }

    @JsonProperty("stop_date_times")
    public List<StopDateTime> getStopDateTimes() {
        return stopDateTimes;
    }

    @JsonProperty("stopDateTimes")
    public void setStopDateTimes(List<StopDateTime> stopDateTimes) {
        this.stopDateTimes = stopDateTimes;
    }

    @JsonProperty("type")
    public SectionTypeEnum getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(SectionTypeEnum type) {
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

    @JsonProperty("dataFreshness")
    public SectionDataFreshnessEnum getDataFreshness() {
        return dataFreshness;
    }

    @JsonProperty("data_freshness")
    public void setDataFreshness(SectionDataFreshnessEnum dataFreshness) {
        this.dataFreshness = dataFreshness;
    }

    @JsonProperty("mode")
    public SectionModeEnum getMode() {
        return mode;
    }

    @JsonProperty("mode")
    public void setMode(SectionModeEnum mode) {
        this.mode = mode;
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
