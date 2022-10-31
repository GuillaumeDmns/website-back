
package com.gdamiens.website.idfm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "RecordedAtTime",
    "LineRef",
    "DirectionRef",
    "DatedVehicleJourneyRef",
    "RouteRef",
    "PublishedLineName",
    "OriginRef",
    "DestinationRef",
    "DestinationName",
    "OperatorRef",
    "ProductCategoryRef",
    "JourneyNote",
    "FirstOrLastJourney",
    "EstimatedCalls",
    "VehicleMode",
    "DirectionName",
    "OriginName",
    "VehicleJourneyName"
})
@Generated("jsonschema2pojo")
public class EstimatedVehicleJourney {

    @JsonProperty("RecordedAtTime")
    private String recordedAtTime;
    @JsonProperty("LineRef")
    private LineRef lineRef;
    @JsonProperty("DirectionRef")
    private DirectionRef directionRef;
    @JsonProperty("DatedVehicleJourneyRef")
    private DatedVehicleJourneyRef datedVehicleJourneyRef;
    @JsonProperty("RouteRef")
    private RouteRef routeRef;
    @JsonProperty("PublishedLineName")
    private List<PublishedLineName> publishedLineName = null;
    @JsonProperty("OriginRef")
    private OriginRef originRef;
    @JsonProperty("DestinationRef")
    private DestinationRef destinationRef;
    @JsonProperty("DestinationName")
    private List<DestinationName> destinationName = null;
    @JsonProperty("OperatorRef")
    private OperatorRef operatorRef;
    @JsonProperty("ProductCategoryRef")
    private ProductCategoryRef productCategoryRef;
    @JsonProperty("JourneyNote")
    private List<JourneyNote> journeyNote = null;
    @JsonProperty("FirstOrLastJourney")
    private String firstOrLastJourney;
    @JsonProperty("EstimatedCalls")
    private EstimatedCalls estimatedCalls;
    @JsonProperty("VehicleMode")
    private List<String> vehicleMode = null;
    @JsonProperty("DirectionName")
    private List<DirectionName> directionName = null;
    @JsonProperty("OriginName")
    private List<OriginName> originName = null;
    @JsonProperty("VehicleJourneyName")
    private List<VehicleJourneyName> vehicleJourneyName = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("RecordedAtTime")
    public String getRecordedAtTime() {
        return recordedAtTime;
    }

    @JsonProperty("RecordedAtTime")
    public void setRecordedAtTime(String recordedAtTime) {
        this.recordedAtTime = recordedAtTime;
    }

    @JsonProperty("LineRef")
    public LineRef getLineRef() {
        return lineRef;
    }

    @JsonProperty("LineRef")
    public void setLineRef(LineRef lineRef) {
        this.lineRef = lineRef;
    }

    @JsonProperty("DirectionRef")
    public DirectionRef getDirectionRef() {
        return directionRef;
    }

    @JsonProperty("DirectionRef")
    public void setDirectionRef(DirectionRef directionRef) {
        this.directionRef = directionRef;
    }

    @JsonProperty("DatedVehicleJourneyRef")
    public DatedVehicleJourneyRef getDatedVehicleJourneyRef() {
        return datedVehicleJourneyRef;
    }

    @JsonProperty("DatedVehicleJourneyRef")
    public void setDatedVehicleJourneyRef(DatedVehicleJourneyRef datedVehicleJourneyRef) {
        this.datedVehicleJourneyRef = datedVehicleJourneyRef;
    }

    @JsonProperty("RouteRef")
    public RouteRef getRouteRef() {
        return routeRef;
    }

    @JsonProperty("RouteRef")
    public void setRouteRef(RouteRef routeRef) {
        this.routeRef = routeRef;
    }

    @JsonProperty("PublishedLineName")
    public List<PublishedLineName> getPublishedLineName() {
        return publishedLineName;
    }

    @JsonProperty("PublishedLineName")
    public void setPublishedLineName(List<PublishedLineName> publishedLineName) {
        this.publishedLineName = publishedLineName;
    }

    @JsonProperty("OriginRef")
    public OriginRef getOriginRef() {
        return originRef;
    }

    @JsonProperty("OriginRef")
    public void setOriginRef(OriginRef originRef) {
        this.originRef = originRef;
    }

    @JsonProperty("DestinationRef")
    public DestinationRef getDestinationRef() {
        return destinationRef;
    }

    @JsonProperty("DestinationRef")
    public void setDestinationRef(DestinationRef destinationRef) {
        this.destinationRef = destinationRef;
    }

    @JsonProperty("DestinationName")
    public List<DestinationName> getDestinationName() {
        return destinationName;
    }

    @JsonProperty("DestinationName")
    public void setDestinationName(List<DestinationName> destinationName) {
        this.destinationName = destinationName;
    }

    @JsonProperty("OperatorRef")
    public OperatorRef getOperatorRef() {
        return operatorRef;
    }

    @JsonProperty("OperatorRef")
    public void setOperatorRef(OperatorRef operatorRef) {
        this.operatorRef = operatorRef;
    }

    @JsonProperty("ProductCategoryRef")
    public ProductCategoryRef getProductCategoryRef() {
        return productCategoryRef;
    }

    @JsonProperty("ProductCategoryRef")
    public void setProductCategoryRef(ProductCategoryRef productCategoryRef) {
        this.productCategoryRef = productCategoryRef;
    }

    @JsonProperty("JourneyNote")
    public List<JourneyNote> getJourneyNote() {
        return journeyNote;
    }

    @JsonProperty("JourneyNote")
    public void setJourneyNote(List<JourneyNote> journeyNote) {
        this.journeyNote = journeyNote;
    }

    @JsonProperty("FirstOrLastJourney")
    public String getFirstOrLastJourney() {
        return firstOrLastJourney;
    }

    @JsonProperty("FirstOrLastJourney")
    public void setFirstOrLastJourney(String firstOrLastJourney) {
        this.firstOrLastJourney = firstOrLastJourney;
    }

    @JsonProperty("EstimatedCalls")
    public EstimatedCalls getEstimatedCalls() {
        return estimatedCalls;
    }

    @JsonProperty("EstimatedCalls")
    public void setEstimatedCalls(EstimatedCalls estimatedCalls) {
        this.estimatedCalls = estimatedCalls;
    }

    @JsonProperty("VehicleMode")
    public List<String> getVehicleMode() {
        return vehicleMode;
    }

    @JsonProperty("VehicleMode")
    public void setVehicleMode(List<String> vehicleMode) {
        this.vehicleMode = vehicleMode;
    }

    @JsonProperty("DirectionName")
    public List<DirectionName> getDirectionName() {
        return directionName;
    }

    @JsonProperty("DirectionName")
    public void setDirectionName(List<DirectionName> directionName) {
        this.directionName = directionName;
    }

    @JsonProperty("OriginName")
    public List<OriginName> getOriginName() {
        return originName;
    }

    @JsonProperty("OriginName")
    public void setOriginName(List<OriginName> originName) {
        this.originName = originName;
    }

    @JsonProperty("VehicleJourneyName")
    public List<VehicleJourneyName> getVehicleJourneyName() {
        return vehicleJourneyName;
    }

    @JsonProperty("VehicleJourneyName")
    public void setVehicleJourneyName(List<VehicleJourneyName> vehicleJourneyName) {
        this.vehicleJourneyName = vehicleJourneyName;
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
