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
import io.swagger.client.model.PtObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PtObjects
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2025-01-15T18:02:06.472Z")
public class PtObjects {
  @SerializedName("error")
  private Error error = null;

  @SerializedName("feed_publishers")
  private List<FeedPublisher> feedPublishers = new ArrayList<FeedPublisher>();

  @SerializedName("disruptions")
  private List<Disruption> disruptions = new ArrayList<Disruption>();

  @SerializedName("pt_objects")
  private List<PtObject> ptObjects = null;

  @SerializedName("links")
  private List<LinkSchema> links = null;

  @SerializedName("context")
  private Context context = null;

  public PtObjects error(Error error) {
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

  public PtObjects feedPublishers(List<FeedPublisher> feedPublishers) {
    this.feedPublishers = feedPublishers;
    return this;
  }

  public PtObjects addFeedPublishersItem(FeedPublisher feedPublishersItem) {
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

  public PtObjects disruptions(List<Disruption> disruptions) {
    this.disruptions = disruptions;
    return this;
  }

  public PtObjects addDisruptionsItem(Disruption disruptionsItem) {
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

  public PtObjects ptObjects(List<PtObject> ptObjects) {
    this.ptObjects = ptObjects;
    return this;
  }

  public PtObjects addPtObjectsItem(PtObject ptObjectsItem) {
    if (this.ptObjects == null) {
      this.ptObjects = new ArrayList<PtObject>();
    }
    this.ptObjects.add(ptObjectsItem);
    return this;
  }

   /**
   * Get ptObjects
   * @return ptObjects
  **/
  @ApiModelProperty(value = "")
  public List<PtObject> getPtObjects() {
    return ptObjects;
  }

  public void setPtObjects(List<PtObject> ptObjects) {
    this.ptObjects = ptObjects;
  }

  public PtObjects links(List<LinkSchema> links) {
    this.links = links;
    return this;
  }

  public PtObjects addLinksItem(LinkSchema linksItem) {
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

  public PtObjects context(Context context) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PtObjects ptObjects = (PtObjects) o;
    return Objects.equals(this.error, ptObjects.error) &&
        Objects.equals(this.feedPublishers, ptObjects.feedPublishers) &&
        Objects.equals(this.disruptions, ptObjects.disruptions) &&
        Objects.equals(this.ptObjects, ptObjects.ptObjects) &&
        Objects.equals(this.links, ptObjects.links) &&
        Objects.equals(this.context, ptObjects.context);
  }

  @Override
  public int hashCode() {
    return Objects.hash(error, feedPublishers, disruptions, ptObjects, links, context);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PtObjects {\n");

    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    feedPublishers: ").append(toIndentedString(feedPublishers)).append("\n");
    sb.append("    disruptions: ").append(toIndentedString(disruptions)).append("\n");
    sb.append("    ptObjects: ").append(toIndentedString(ptObjects)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
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

