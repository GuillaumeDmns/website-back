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
import io.swagger.client.model.BetaEndpoints;
import io.swagger.client.model.Context;
import io.swagger.client.model.Disruption;
import io.swagger.client.model.Error;
import io.swagger.client.model.FeedPublisher;
import io.swagger.client.model.LineReport;
import io.swagger.client.model.LinkSchema;
import io.swagger.client.model.Note;
import io.swagger.client.model.Pagination;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * LineReports
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2025-01-15T18:02:06.472Z")
public class LineReports {
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

  @SerializedName("line_reports")
  private List<LineReport> lineReports = new ArrayList<LineReport>();

  @SerializedName("warnings")
  private List<BetaEndpoints> warnings = new ArrayList<BetaEndpoints>();

  public LineReports pagination(Pagination pagination) {
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

  public LineReports error(Error error) {
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

  public LineReports feedPublishers(List<FeedPublisher> feedPublishers) {
    this.feedPublishers = feedPublishers;
    return this;
  }

  public LineReports addFeedPublishersItem(FeedPublisher feedPublishersItem) {
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

  public LineReports disruptions(List<Disruption> disruptions) {
    this.disruptions = disruptions;
    return this;
  }

  public LineReports addDisruptionsItem(Disruption disruptionsItem) {
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

  public LineReports notes(List<Note> notes) {
    this.notes = notes;
    return this;
  }

  public LineReports addNotesItem(Note notesItem) {
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

  public LineReports links(List<LinkSchema> links) {
    this.links = links;
    return this;
  }

  public LineReports addLinksItem(LinkSchema linksItem) {
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

  public LineReports context(Context context) {
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

  public LineReports lineReports(List<LineReport> lineReports) {
    this.lineReports = lineReports;
    return this;
  }

  public LineReports addLineReportsItem(LineReport lineReportsItem) {
    this.lineReports.add(lineReportsItem);
    return this;
  }

   /**
   * Get lineReports
   * @return lineReports
  **/
  @ApiModelProperty(required = true, value = "")
  public List<LineReport> getLineReports() {
    return lineReports;
  }

  public void setLineReports(List<LineReport> lineReports) {
    this.lineReports = lineReports;
  }

  public LineReports warnings(List<BetaEndpoints> warnings) {
    this.warnings = warnings;
    return this;
  }

  public LineReports addWarningsItem(BetaEndpoints warningsItem) {
    this.warnings.add(warningsItem);
    return this;
  }

   /**
   * Get warnings
   * @return warnings
  **/
  @ApiModelProperty(required = true, value = "")
  public List<BetaEndpoints> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<BetaEndpoints> warnings) {
    this.warnings = warnings;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LineReports lineReports = (LineReports) o;
    return Objects.equals(this.pagination, lineReports.pagination) &&
        Objects.equals(this.error, lineReports.error) &&
        Objects.equals(this.feedPublishers, lineReports.feedPublishers) &&
        Objects.equals(this.disruptions, lineReports.disruptions) &&
        Objects.equals(this.notes, lineReports.notes) &&
        Objects.equals(this.links, lineReports.links) &&
        Objects.equals(this.context, lineReports.context) &&
        Objects.equals(this.lineReports, lineReports.lineReports) &&
        Objects.equals(this.warnings, lineReports.warnings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pagination, error, feedPublishers, disruptions, notes, links, context, lineReports, warnings);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LineReports {\n");

    sb.append("    pagination: ").append(toIndentedString(pagination)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    feedPublishers: ").append(toIndentedString(feedPublishers)).append("\n");
    sb.append("    disruptions: ").append(toIndentedString(disruptions)).append("\n");
    sb.append("    notes: ").append(toIndentedString(notes)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    lineReports: ").append(toIndentedString(lineReports)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
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

