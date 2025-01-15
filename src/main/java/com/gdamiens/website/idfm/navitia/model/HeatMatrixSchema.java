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
import io.swagger.client.model.LineHeadersSchema;
import io.swagger.client.model.LinesSchema;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HeatMatrixSchema
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2025-01-15T18:02:06.472Z")
public class HeatMatrixSchema {
  @SerializedName("line_headers")
  private List<LineHeadersSchema> lineHeaders = new ArrayList<LineHeadersSchema>();

  @SerializedName("lines")
  private List<LinesSchema> lines = new ArrayList<LinesSchema>();

  public HeatMatrixSchema lineHeaders(List<LineHeadersSchema> lineHeaders) {
    this.lineHeaders = lineHeaders;
    return this;
  }

  public HeatMatrixSchema addLineHeadersItem(LineHeadersSchema lineHeadersItem) {
    this.lineHeaders.add(lineHeadersItem);
    return this;
  }

   /**
   * Get lineHeaders
   * @return lineHeaders
  **/
  @ApiModelProperty(required = true, value = "")
  public List<LineHeadersSchema> getLineHeaders() {
    return lineHeaders;
  }

  public void setLineHeaders(List<LineHeadersSchema> lineHeaders) {
    this.lineHeaders = lineHeaders;
  }

  public HeatMatrixSchema lines(List<LinesSchema> lines) {
    this.lines = lines;
    return this;
  }

  public HeatMatrixSchema addLinesItem(LinesSchema linesItem) {
    this.lines.add(linesItem);
    return this;
  }

   /**
   * Get lines
   * @return lines
  **/
  @ApiModelProperty(required = true, value = "")
  public List<LinesSchema> getLines() {
    return lines;
  }

  public void setLines(List<LinesSchema> lines) {
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
    HeatMatrixSchema heatMatrixSchema = (HeatMatrixSchema) o;
    return Objects.equals(this.lineHeaders, heatMatrixSchema.lineHeaders) &&
        Objects.equals(this.lines, heatMatrixSchema.lines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lineHeaders, lines);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HeatMatrixSchema {\n");

    sb.append("    lineHeaders: ").append(toIndentedString(lineHeaders)).append("\n");
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

