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
import io.swagger.client.model.FreeFloatingsCoord;
import java.io.IOException;

/**
 * FreeFloatingsFreeFloatings
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2025-01-15T18:02:06.472Z")
public class FreeFloatingsFreeFloatings {
  @SerializedName("public_id")
  private String publicId = null;

  @SerializedName("provider_name")
  private String providerName = null;

  @SerializedName("id")
  private String id = null;

  /**
   * Gets or Sets type
   */
  @JsonAdapter(TypeEnum.Adapter.class)
  public enum TypeEnum {
    BIKE("BIKE"),

    SCOOTER("SCOOTER"),

    MOTORSCOOTER("MOTORSCOOTER"),

    STATION("STATION"),

    CAR("CAR"),

    OTHER("OTHER");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public static class Adapter extends TypeAdapter<TypeEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final TypeEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public TypeEnum read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return TypeEnum.fromValue(String.valueOf(value));
      }
    }
  }

  @SerializedName("type")
  private TypeEnum type = null;

  @SerializedName("propulsion")
  private String propulsion = null;

  @SerializedName("battery")
  private Integer battery = null;

  @SerializedName("distance")
  private Integer distance = null;

  @SerializedName("deeplink")
  private String deeplink = null;

  @SerializedName("coord")
  private FreeFloatingsCoord coord = null;

  public FreeFloatingsFreeFloatings publicId(String publicId) {
    this.publicId = publicId;
    return this;
  }

   /**
   * Get publicId
   * @return publicId
  **/
  @ApiModelProperty(value = "")
  public String getPublicId() {
    return publicId;
  }

  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  public FreeFloatingsFreeFloatings providerName(String providerName) {
    this.providerName = providerName;
    return this;
  }

   /**
   * Get providerName
   * @return providerName
  **/
  @ApiModelProperty(value = "")
  public String getProviderName() {
    return providerName;
  }

  public void setProviderName(String providerName) {
    this.providerName = providerName;
  }

  public FreeFloatingsFreeFloatings id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public FreeFloatingsFreeFloatings type(TypeEnum type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(value = "")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public FreeFloatingsFreeFloatings propulsion(String propulsion) {
    this.propulsion = propulsion;
    return this;
  }

   /**
   * Get propulsion
   * @return propulsion
  **/
  @ApiModelProperty(value = "")
  public String getPropulsion() {
    return propulsion;
  }

  public void setPropulsion(String propulsion) {
    this.propulsion = propulsion;
  }

  public FreeFloatingsFreeFloatings battery(Integer battery) {
    this.battery = battery;
    return this;
  }

   /**
   * Get battery
   * @return battery
  **/
  @ApiModelProperty(value = "")
  public Integer getBattery() {
    return battery;
  }

  public void setBattery(Integer battery) {
    this.battery = battery;
  }

  public FreeFloatingsFreeFloatings distance(Integer distance) {
    this.distance = distance;
    return this;
  }

   /**
   * Get distance
   * @return distance
  **/
  @ApiModelProperty(value = "")
  public Integer getDistance() {
    return distance;
  }

  public void setDistance(Integer distance) {
    this.distance = distance;
  }

  public FreeFloatingsFreeFloatings deeplink(String deeplink) {
    this.deeplink = deeplink;
    return this;
  }

   /**
   * Get deeplink
   * @return deeplink
  **/
  @ApiModelProperty(value = "")
  public String getDeeplink() {
    return deeplink;
  }

  public void setDeeplink(String deeplink) {
    this.deeplink = deeplink;
  }

  public FreeFloatingsFreeFloatings coord(FreeFloatingsCoord coord) {
    this.coord = coord;
    return this;
  }

   /**
   * Get coord
   * @return coord
  **/
  @ApiModelProperty(value = "")
  public FreeFloatingsCoord getCoord() {
    return coord;
  }

  public void setCoord(FreeFloatingsCoord coord) {
    this.coord = coord;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FreeFloatingsFreeFloatings freeFloatingsFreeFloatings = (FreeFloatingsFreeFloatings) o;
    return Objects.equals(this.publicId, freeFloatingsFreeFloatings.publicId) &&
        Objects.equals(this.providerName, freeFloatingsFreeFloatings.providerName) &&
        Objects.equals(this.id, freeFloatingsFreeFloatings.id) &&
        Objects.equals(this.type, freeFloatingsFreeFloatings.type) &&
        Objects.equals(this.propulsion, freeFloatingsFreeFloatings.propulsion) &&
        Objects.equals(this.battery, freeFloatingsFreeFloatings.battery) &&
        Objects.equals(this.distance, freeFloatingsFreeFloatings.distance) &&
        Objects.equals(this.deeplink, freeFloatingsFreeFloatings.deeplink) &&
        Objects.equals(this.coord, freeFloatingsFreeFloatings.coord);
  }

  @Override
  public int hashCode() {
    return Objects.hash(publicId, providerName, id, type, propulsion, battery, distance, deeplink, coord);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FreeFloatingsFreeFloatings {\n");

    sb.append("    publicId: ").append(toIndentedString(publicId)).append("\n");
    sb.append("    providerName: ").append(toIndentedString(providerName)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    propulsion: ").append(toIndentedString(propulsion)).append("\n");
    sb.append("    battery: ").append(toIndentedString(battery)).append("\n");
    sb.append("    distance: ").append(toIndentedString(distance)).append("\n");
    sb.append("    deeplink: ").append(toIndentedString(deeplink)).append("\n");
    sb.append("    coord: ").append(toIndentedString(coord)).append("\n");
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

