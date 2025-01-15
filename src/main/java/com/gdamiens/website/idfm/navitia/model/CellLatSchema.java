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
import java.io.IOException;

/**
 * CellLatSchema
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2025-01-15T18:02:06.472Z")
public class CellLatSchema {
  @SerializedName("min_lat")
  private Float minLat = null;

  @SerializedName("max_lat")
  private Float maxLat = null;

  @SerializedName("center_lat")
  private Float centerLat = null;

  public CellLatSchema minLat(Float minLat) {
    this.minLat = minLat;
    return this;
  }

   /**
   * Get minLat
   * @return minLat
  **/
  @ApiModelProperty(value = "")
  public Float getMinLat() {
    return minLat;
  }

  public void setMinLat(Float minLat) {
    this.minLat = minLat;
  }

  public CellLatSchema maxLat(Float maxLat) {
    this.maxLat = maxLat;
    return this;
  }

   /**
   * Get maxLat
   * @return maxLat
  **/
  @ApiModelProperty(value = "")
  public Float getMaxLat() {
    return maxLat;
  }

  public void setMaxLat(Float maxLat) {
    this.maxLat = maxLat;
  }

  public CellLatSchema centerLat(Float centerLat) {
    this.centerLat = centerLat;
    return this;
  }

   /**
   * Get centerLat
   * @return centerLat
  **/
  @ApiModelProperty(value = "")
  public Float getCenterLat() {
    return centerLat;
  }

  public void setCenterLat(Float centerLat) {
    this.centerLat = centerLat;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CellLatSchema cellLatSchema = (CellLatSchema) o;
    return Objects.equals(this.minLat, cellLatSchema.minLat) &&
        Objects.equals(this.maxLat, cellLatSchema.maxLat) &&
        Objects.equals(this.centerLat, cellLatSchema.centerLat);
  }

  @Override
  public int hashCode() {
    return Objects.hash(minLat, maxLat, centerLat);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CellLatSchema {\n");

    sb.append("    minLat: ").append(toIndentedString(minLat)).append("\n");
    sb.append("    maxLat: ").append(toIndentedString(maxLat)).append("\n");
    sb.append("    centerLat: ").append(toIndentedString(centerLat)).append("\n");
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

