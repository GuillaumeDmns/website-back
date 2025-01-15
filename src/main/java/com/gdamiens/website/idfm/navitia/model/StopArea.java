/*
 * navitia
 *      navitia.io is the open API for building cool stuff with mobility data. It provides the following services      * journeys computation     * line schedules     * next departures     * exploration of public transport data / search places     * and sexy things such as isochrones      navitia is a HATEOAS API that returns JSON formated results
 *
 * OpenAPI spec version: v15.75.4
 * Contact: navitia@googlegroups.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.gdamiens.website.idfm.navitia.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.client.model.Admin;
import io.swagger.client.model.Code;
import io.swagger.client.model.Comment;
import io.swagger.client.model.CommercialMode;
import io.swagger.client.model.Coord;
import io.swagger.client.model.Line;
import io.swagger.client.model.LinkSchema;
import io.swagger.client.model.PhysicalMode;
import io.swagger.client.model.StopPoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * StopArea
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2025-01-15T18:02:06.472Z")
public class StopArea {
  @SerializedName("id")
  private String id = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("comments")
  private List<Comment> comments = null;

  @SerializedName("comment")
  private String comment = null;

  @SerializedName("codes")
  private List<Code> codes = null;

  @SerializedName("timezone")
  private String timezone = null;

  @SerializedName("label")
  private String label = null;

  @SerializedName("coord")
  private Coord coord = null;

  @SerializedName("links")
  private List<LinkSchema> links = new ArrayList<LinkSchema>();

  @SerializedName("commercial_modes")
  private List<CommercialMode> commercialModes = null;

  @SerializedName("physical_modes")
  private List<PhysicalMode> physicalModes = null;

  @SerializedName("administrative_regions")
  private List<Admin> administrativeRegions = null;

  @SerializedName("stop_points")
  private List<StopPoint> stopPoints = null;

  @SerializedName("lines")
  private List<Line> lines = null;

  public StopArea id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Identifier of the object
   * @return id
  **/
  @ApiModelProperty(required = true, value = "Identifier of the object")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public StopArea name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Name of the object
   * @return name
  **/
  @ApiModelProperty(required = true, value = "Name of the object")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public StopArea comments(List<Comment> comments) {
    this.comments = comments;
    return this;
  }

  public StopArea addCommentsItem(Comment commentsItem) {
    if (this.comments == null) {
      this.comments = new ArrayList<Comment>();
    }
    this.comments.add(commentsItem);
    return this;
  }

   /**
   * Get comments
   * @return comments
  **/
  @ApiModelProperty(value = "")
  public List<Comment> getComments() {
    return comments;
  }

  public void setComments(List<Comment> comments) {
    this.comments = comments;
  }

  public StopArea comment(String comment) {
    this.comment = comment;
    return this;
  }

   /**
   * Get comment
   * @return comment
  **/
  @ApiModelProperty(value = "")
  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public StopArea codes(List<Code> codes) {
    this.codes = codes;
    return this;
  }

  public StopArea addCodesItem(Code codesItem) {
    if (this.codes == null) {
      this.codes = new ArrayList<Code>();
    }
    this.codes.add(codesItem);
    return this;
  }

   /**
   * Get codes
   * @return codes
  **/
  @ApiModelProperty(value = "")
  public List<Code> getCodes() {
    return codes;
  }

  public void setCodes(List<Code> codes) {
    this.codes = codes;
  }

  public StopArea timezone(String timezone) {
    this.timezone = timezone;
    return this;
  }

   /**
   * Get timezone
   * @return timezone
  **/
  @ApiModelProperty(value = "")
  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public StopArea label(String label) {
    this.label = label;
    return this;
  }

   /**
   *  Label of the stop area. The name is directly taken from the data whereas the label is  something we compute for better traveler information. If you don&#39;t know what to display, display the label.
   * @return label
  **/
  @ApiModelProperty(value = " Label of the stop area. The name is directly taken from the data whereas the label is  something we compute for better traveler information. If you don't know what to display, display the label. ")
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public StopArea coord(Coord coord) {
    this.coord = coord;
    return this;
  }

   /**
   * Get coord
   * @return coord
  **/
  @ApiModelProperty(value = "")
  public Coord getCoord() {
    return coord;
  }

  public void setCoord(Coord coord) {
    this.coord = coord;
  }

  public StopArea links(List<LinkSchema> links) {
    this.links = links;
    return this;
  }

  public StopArea addLinksItem(LinkSchema linksItem) {
    this.links.add(linksItem);
    return this;
  }

   /**
   * Get links
   * @return links
  **/
  @ApiModelProperty(required = true, value = "")
  public List<LinkSchema> getLinks() {
    return links;
  }

  public void setLinks(List<LinkSchema> links) {
    this.links = links;
  }

  public StopArea commercialModes(List<CommercialMode> commercialModes) {
    this.commercialModes = commercialModes;
    return this;
  }

  public StopArea addCommercialModesItem(CommercialMode commercialModesItem) {
    if (this.commercialModes == null) {
      this.commercialModes = new ArrayList<CommercialMode>();
    }
    this.commercialModes.add(commercialModesItem);
    return this;
  }

   /**
   * Get commercialModes
   * @return commercialModes
  **/
  @ApiModelProperty(value = "")
  public List<CommercialMode> getCommercialModes() {
    return commercialModes;
  }

  public void setCommercialModes(List<CommercialMode> commercialModes) {
    this.commercialModes = commercialModes;
  }

  public StopArea physicalModes(List<PhysicalMode> physicalModes) {
    this.physicalModes = physicalModes;
    return this;
  }

  public StopArea addPhysicalModesItem(PhysicalMode physicalModesItem) {
    if (this.physicalModes == null) {
      this.physicalModes = new ArrayList<PhysicalMode>();
    }
    this.physicalModes.add(physicalModesItem);
    return this;
  }

   /**
   * Get physicalModes
   * @return physicalModes
  **/
  @ApiModelProperty(value = "")
  public List<PhysicalMode> getPhysicalModes() {
    return physicalModes;
  }

  public void setPhysicalModes(List<PhysicalMode> physicalModes) {
    this.physicalModes = physicalModes;
  }

  public StopArea administrativeRegions(List<Admin> administrativeRegions) {
    this.administrativeRegions = administrativeRegions;
    return this;
  }

  public StopArea addAdministrativeRegionsItem(Admin administrativeRegionsItem) {
    if (this.administrativeRegions == null) {
      this.administrativeRegions = new ArrayList<Admin>();
    }
    this.administrativeRegions.add(administrativeRegionsItem);
    return this;
  }

   /**
   * Get administrativeRegions
   * @return administrativeRegions
  **/
  @ApiModelProperty(value = "")
  public List<Admin> getAdministrativeRegions() {
    return administrativeRegions;
  }

  public void setAdministrativeRegions(List<Admin> administrativeRegions) {
    this.administrativeRegions = administrativeRegions;
  }

  public StopArea stopPoints(List<StopPoint> stopPoints) {
    this.stopPoints = stopPoints;
    return this;
  }

  public StopArea addStopPointsItem(StopPoint stopPointsItem) {
    if (this.stopPoints == null) {
      this.stopPoints = new ArrayList<StopPoint>();
    }
    this.stopPoints.add(stopPointsItem);
    return this;
  }

   /**
   * Get stopPoints
   * @return stopPoints
  **/
  @ApiModelProperty(value = "")
  public List<StopPoint> getStopPoints() {
    return stopPoints;
  }

  public void setStopPoints(List<StopPoint> stopPoints) {
    this.stopPoints = stopPoints;
  }

  public StopArea lines(List<Line> lines) {
    this.lines = lines;
    return this;
  }

  public StopArea addLinesItem(Line linesItem) {
    if (this.lines == null) {
      this.lines = new ArrayList<Line>();
    }
    this.lines.add(linesItem);
    return this;
  }

   /**
   * Get lines
   * @return lines
  **/
  @ApiModelProperty(value = "")
  public List<Line> getLines() {
    return lines;
  }

  public void setLines(List<Line> lines) {
    this.lines = lines;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StopArea stopArea = (StopArea) o;
    return Objects.equals(this.id, stopArea.id) &&
        Objects.equals(this.name, stopArea.name) &&
        Objects.equals(this.comments, stopArea.comments) &&
        Objects.equals(this.comment, stopArea.comment) &&
        Objects.equals(this.codes, stopArea.codes) &&
        Objects.equals(this.timezone, stopArea.timezone) &&
        Objects.equals(this.label, stopArea.label) &&
        Objects.equals(this.coord, stopArea.coord) &&
        Objects.equals(this.links, stopArea.links) &&
        Objects.equals(this.commercialModes, stopArea.commercialModes) &&
        Objects.equals(this.physicalModes, stopArea.physicalModes) &&
        Objects.equals(this.administrativeRegions, stopArea.administrativeRegions) &&
        Objects.equals(this.stopPoints, stopArea.stopPoints) &&
        Objects.equals(this.lines, stopArea.lines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, comments, comment, codes, timezone, label, coord, links, commercialModes, physicalModes, administrativeRegions, stopPoints, lines);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StopArea {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    comments: ").append(toIndentedString(comments)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
    sb.append("    codes: ").append(toIndentedString(codes)).append("\n");
    sb.append("    timezone: ").append(toIndentedString(timezone)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    coord: ").append(toIndentedString(coord)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    commercialModes: ").append(toIndentedString(commercialModes)).append("\n");
    sb.append("    physicalModes: ").append(toIndentedString(physicalModes)).append("\n");
    sb.append("    administrativeRegions: ").append(toIndentedString(administrativeRegions)).append("\n");
    sb.append("    stopPoints: ").append(toIndentedString(stopPoints)).append("\n");
    sb.append("    lines: ").append(toIndentedString(lines)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

