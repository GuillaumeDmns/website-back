package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "comments",
    "comment",
    "codes",
    "label",
    "coord",
    "links",
    "commercial_modes",
    "physical_modes",
    "administrative_regions",
    "stop_area",
    "equipments",
    "address",
    "fare_zone",
    "equipment_details",
    "lines",
    "access_points"
})
@Generated("jsonschema2pojo")
public class StopPoint {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("comments")
    private List<Comment> comments = null;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("codes")
    private List<Code> codes = null;

    @JsonProperty("label")
    private String label;

    @JsonProperty("coord")
    private Coord coord;

    @JsonProperty("links")
    private List<LinkSchema> links;

    private List<CommercialMode> commercialModes = null;

    private List<PhysicalMode> physicalModes = null;

    private List<Admin> administrativeRegions = null;

    private StopArea stopArea;

    @JsonProperty("equipments")
    private EquipmentsEnum equipments;

    @JsonProperty("address")
    private Address address;

    private FareZone fareZone;

    private List<EquipmentDetails> equipmentDetails = null;

    @JsonProperty("lines")
    private List<Line> lines = null;

    private List<PathWay> accessPoints = null;

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

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("comments")
    public List<Comment> getComments() {
        return comments;
    }

    @JsonProperty("comments")
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    @JsonProperty("codes")
    public List<Code> getCodes() {
        return codes;
    }

    @JsonProperty("codes")
    public void setCodes(List<Code> codes) {
        this.codes = codes;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("coord")
    public Coord getCoord() {
        return coord;
    }

    @JsonProperty("coord")
    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    @JsonProperty("links")
    public List<LinkSchema> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<LinkSchema> links) {
        this.links = links;
    }

    @JsonProperty("commercialModes")
    public List<CommercialMode> getCommercialModes() {
        return commercialModes;
    }

    @JsonProperty("commercial_modes")
    public void setCommercialModes(List<CommercialMode> commercialModes) {
        this.commercialModes = commercialModes;
    }

    @JsonProperty("physicalModes")
    public List<PhysicalMode> getPhysicalModes() {
        return physicalModes;
    }

    @JsonProperty("physical_modes")
    public void setPhysicalModes(List<PhysicalMode> physicalModes) {
        this.physicalModes = physicalModes;
    }

    @JsonProperty("administrativeRegions")
    public List<Admin> getAdministrativeRegions() {
        return administrativeRegions;
    }

    @JsonProperty("administrative_regions")
    public void setAdministrativeRegions(List<Admin> administrativeRegions) {
        this.administrativeRegions = administrativeRegions;
    }

    @JsonProperty("stopArea")
    public StopArea getStopArea() {
        return stopArea;
    }

    @JsonProperty("stop_area")
    public void setStopArea(StopArea stopArea) {
        this.stopArea = stopArea;
    }

    @JsonProperty("equipments")
    public EquipmentsEnum getEquipments() {
        return equipments;
    }

    @JsonProperty("equipments")
    public void setEquipments(EquipmentsEnum equipments) {
        this.equipments = equipments;
    }

    @JsonProperty("address")
    public Address getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(Address address) {
        this.address = address;
    }

    @JsonProperty("fareZone")
    public FareZone getFareZone() {
        return fareZone;
    }

    @JsonProperty("fare_zone")
    public void setFareZone(FareZone fareZone) {
        this.fareZone = fareZone;
    }

    @JsonProperty("equipmentDetails")
    public List<EquipmentDetails> getEquipmentDetails() {
        return equipmentDetails;
    }

    @JsonProperty("equipment_details")
    public void setEquipmentDetails(List<EquipmentDetails> equipmentDetails) {
        this.equipmentDetails = equipmentDetails;
    }

    @JsonProperty("lines")
    public List<Line> getLines() {
        return lines;
    }

    @JsonProperty("lines")
    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    @JsonProperty("accessPoints")
    public List<PathWay> getAccessPoints() {
        return accessPoints;
    }

    @JsonProperty("access_points")
    public void setAccessPoints(List<PathWay> accessPoints) {
        this.accessPoints = accessPoints;
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
