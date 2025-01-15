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
import io.swagger.client.model.Context;
import io.swagger.client.model.Disruption;
import io.swagger.client.model.Error;
import io.swagger.client.model.FeedPublisher;
import io.swagger.client.model.LinkSchema;
import io.swagger.client.model.Note;
import io.swagger.client.model.Pagination;
import io.swagger.client.model.PhysicalMode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PhysicalModes
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2025-01-15T18:02:06.472Z")
public class PhysicalModes {
  @SerializedName("pagination")
  private Pagination pagination = null;

  @SerializedName("error")
  private Error error = null;

  @SerializedName("feed_publishers")
  private List<FeedPublisher> feedPublishers = new ArrayList<FeedPublisher>();

  @SerializedName("disruptions")
  private List<Disruption> disruptions = new ArrayList<Disruption>();

  @SerializedName("notes")
  private List<Note> notes = null;

  @SerializedName("links")
  private List<LinkSchema> links = null;

  @SerializedName("context")
  private Context context = null;

  @SerializedName("physical_modes")
  private List<PhysicalMode> physicalModes = null;

  public PhysicalModes pagination(Pagination pagination) {
    this.pagination = pagination;
    return this;
  }

   /**
   * Get pagination
   * @return pagination
  **/
  @ApiModelProperty(required = true, value = "")
  public Pagination getPagination() {
    return pagination;
  }

  public void setPagination(Pagination pagination) {
    this.pagination = pagination;
  }

  public PhysicalModes error(Error error) {
    this.error = error;
    return this;
  }

   /**
   * Get error
   * @return error
  **/
  @ApiModelProperty(value = "")
  public Error getError() {
    return error;
  }

  public void setError(Error error) {
    this.error = error;
  }

  public PhysicalModes feedPublishers(List<FeedPublisher> feedPublishers) {
    this.feedPublishers = feedPublishers;
    return this;
  }

  public PhysicalModes addFeedPublishersItem(FeedPublisher feedPublishersItem) {
    this.feedPublishers.add(feedPublishersItem);
    return this;
  }

   /**
   * Get feedPublishers
   * @return feedPublishers
  **/
  @ApiModelProperty(required = true, value = "")
  public List<FeedPublisher> getFeedPublishers() {
    return feedPublishers;
  }

  public void setFeedPublishers(List<FeedPublisher> feedPublishers) {
    this.feedPublishers = feedPublishers;
  }

  public PhysicalModes disruptions(List<Disruption> disruptions) {
    this.disruptions = disruptions;
    return this;
  }

  public PhysicalModes addDisruptionsItem(Disruption disruptionsItem) {
    this.disruptions.add(disruptionsItem);
    return this;
  }

   /**
   * Get disruptions
   * @return disruptions
  **/
  @ApiModelProperty(required = true, value = "")
  public List<Disruption> getDisruptions() {
    return disruptions;
  }

  public void setDisruptions(List<Disruption> disruptions) {
    this.disruptions = disruptions;
  }

  public PhysicalModes notes(List<Note> notes) {
    this.notes = notes;
    return this;
  }

  public PhysicalModes addNotesItem(Note notesItem) {
    if (this.notes == null) {
      this.notes = new ArrayList<Note>();
    }
    this.notes.add(notesItem);
    return this;
  }

   /**
   * Get notes
   * @return notes
  **/
  @ApiModelProperty(value = "")
  public List<Note> getNotes() {
    return notes;
  }

  public void setNotes(List<Note> notes) {
    this.notes = notes;
  }

  public PhysicalModes links(List<LinkSchema> links) {
    this.links = links;
    return this;
  }

  public PhysicalModes addLinksItem(LinkSchema linksItem) {
    if (this.links == null) {
      this.links = new ArrayList<LinkSchema>();
    }
    this.links.add(linksItem);
    return this;
  }

   /**
   * Get links
   * @return links
  **/
  @ApiModelProperty(value = "")
  public List<LinkSchema> getLinks() {
    return links;
  }

  public void setLinks(List<LinkSchema> links) {
    this.links = links;
  }

  public PhysicalModes context(Context context) {
    this.context = context;
    return this;
  }

   /**
   * Get context
   * @return context
  **/
  @ApiModelProperty(value = "")
  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public PhysicalModes physicalModes(List<PhysicalMode> physicalModes) {
    this.physicalModes = physicalModes;
    return this;
  }

  public PhysicalModes addPhysicalModesItem(PhysicalMode physicalModesItem) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PhysicalModes physicalModes = (PhysicalModes) o;
    return Objects.equals(this.pagination, physicalModes.pagination) &&
        Objects.equals(this.error, physicalModes.error) &&
        Objects.equals(this.feedPublishers, physicalModes.feedPublishers) &&
        Objects.equals(this.disruptions, physicalModes.disruptions) &&
        Objects.equals(this.notes, physicalModes.notes) &&
        Objects.equals(this.links, physicalModes.links) &&
        Objects.equals(this.context, physicalModes.context) &&
        Objects.equals(this.physicalModes, physicalModes.physicalModes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pagination, error, feedPublishers, disruptions, notes, links, context, physicalModes);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PhysicalModes {\n");

    sb.append("    pagination: ").append(toIndentedString(pagination)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    feedPublishers: ").append(toIndentedString(feedPublishers)).append("\n");
    sb.append("    disruptions: ").append(toIndentedString(disruptions)).append("\n");
    sb.append("    notes: ").append(toIndentedString(notes)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    physicalModes: ").append(toIndentedString(physicalModes)).append("\n");
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

