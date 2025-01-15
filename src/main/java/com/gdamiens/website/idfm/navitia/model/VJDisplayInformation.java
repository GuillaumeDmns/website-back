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
import io.swagger.client.model.LinkSchema;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * VJDisplayInformation
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2025-01-15T18:02:06.472Z")
public class VJDisplayInformation {
  @SerializedName("commercial_mode")
  private String commercialMode = null;

  @SerializedName("network")
  private String network = null;

  @SerializedName("direction")
  private String direction = null;

  @SerializedName("label")
  private String label = null;

  @SerializedName("color")
  private String color = null;

  @SerializedName("code")
  private String code = null;

  @SerializedName("headsign")
  private String headsign = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("links")
  private List<LinkSchema> links = new ArrayList<LinkSchema>();

  @SerializedName("text_color")
  private String textColor = null;

  @SerializedName("trip_short_name")
  private String tripShortName = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("physical_mode")
  private String physicalMode = null;

  /**
   * Gets or Sets equipments
   */
  @JsonAdapter(EquipmentsEnum.Adapter.class)
  public enum EquipmentsEnum {
    WHEELCHAIR_ACCESSIBILITY("has_wheelchair_accessibility"),

    BIKE_ACCEPTED("has_bike_accepted"),

    AIR_CONDITIONED("has_air_conditioned"),

    VISUAL_ANNOUNCEMENT("has_visual_announcement"),

    AUDIBLE_ANNOUNCEMENT("has_audible_announcement"),

    APPROPRIATE_ESCORT("has_appropriate_escort"),

    APPROPRIATE_SIGNAGE("has_appropriate_signage"),

    SCHOOL_VEHICLE("has_school_vehicle"),

    WHEELCHAIR_BOARDING("has_wheelchair_boarding"),

    SHELTERED("has_sheltered"),

    ELEVATOR("has_elevator"),

    ESCALATOR("has_escalator"),

    BIKE_DEPOT("has_bike_depot");

    private String value;

    EquipmentsEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static EquipmentsEnum fromValue(String text) {
      for (EquipmentsEnum b : EquipmentsEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public static class Adapter extends TypeAdapter<EquipmentsEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final EquipmentsEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public EquipmentsEnum read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return EquipmentsEnum.fromValue(String.valueOf(value));
      }
    }
  }

  @SerializedName("equipments")
  private List<EquipmentsEnum> equipments = new ArrayList<EquipmentsEnum>();

  @SerializedName("headsigns")
  private List<String> headsigns = null;

  public VJDisplayInformation commercialMode(String commercialMode) {
    this.commercialMode = commercialMode;
    return this;
  }

   /**
   * Get commercialMode
   * @return commercialMode
  **/
  @ApiModelProperty(value = "")
  public String getCommercialMode() {
    return commercialMode;
  }

  public void setCommercialMode(String commercialMode) {
    this.commercialMode = commercialMode;
  }

  public VJDisplayInformation network(String network) {
    this.network = network;
    return this;
  }

   /**
   * Get network
   * @return network
  **/
  @ApiModelProperty(value = "")
  public String getNetwork() {
    return network;
  }

  public void setNetwork(String network) {
    this.network = network;
  }

  public VJDisplayInformation direction(String direction) {
    this.direction = direction;
    return this;
  }

   /**
   * Get direction
   * @return direction
  **/
  @ApiModelProperty(required = true, value = "")
  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public VJDisplayInformation label(String label) {
    this.label = label;
    return this;
  }

   /**
   * Get label
   * @return label
  **/
  @ApiModelProperty(value = "")
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public VJDisplayInformation color(String color) {
    this.color = color;
    return this;
  }

   /**
   * Get color
   * @return color
  **/
  @ApiModelProperty(value = "")
  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public VJDisplayInformation code(String code) {
    this.code = code;
    return this;
  }

   /**
   * Get code
   * @return code
  **/
  @ApiModelProperty(value = "")
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public VJDisplayInformation headsign(String headsign) {
    this.headsign = headsign;
    return this;
  }

   /**
   * Get headsign
   * @return headsign
  **/
  @ApiModelProperty(required = true, value = "")
  public String getHeadsign() {
    return headsign;
  }

  public void setHeadsign(String headsign) {
    this.headsign = headsign;
  }

  public VJDisplayInformation name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public VJDisplayInformation links(List<LinkSchema> links) {
    this.links = links;
    return this;
  }

  public VJDisplayInformation addLinksItem(LinkSchema linksItem) {
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

  public VJDisplayInformation textColor(String textColor) {
    this.textColor = textColor;
    return this;
  }

   /**
   * Get textColor
   * @return textColor
  **/
  @ApiModelProperty(value = "")
  public String getTextColor() {
    return textColor;
  }

  public void setTextColor(String textColor) {
    this.textColor = textColor;
  }

  public VJDisplayInformation tripShortName(String tripShortName) {
    this.tripShortName = tripShortName;
    return this;
  }

   /**
   * Get tripShortName
   * @return tripShortName
  **/
  @ApiModelProperty(value = "")
  public String getTripShortName() {
    return tripShortName;
  }

  public void setTripShortName(String tripShortName) {
    this.tripShortName = tripShortName;
  }

  public VJDisplayInformation description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public VJDisplayInformation physicalMode(String physicalMode) {
    this.physicalMode = physicalMode;
    return this;
  }

   /**
   * Get physicalMode
   * @return physicalMode
  **/
  @ApiModelProperty(value = "")
  public String getPhysicalMode() {
    return physicalMode;
  }

  public void setPhysicalMode(String physicalMode) {
    this.physicalMode = physicalMode;
  }

  public VJDisplayInformation equipments(List<EquipmentsEnum> equipments) {
    this.equipments = equipments;
    return this;
  }

  public VJDisplayInformation addEquipmentsItem(EquipmentsEnum equipmentsItem) {
    this.equipments.add(equipmentsItem);
    return this;
  }

   /**
   * Get equipments
   * @return equipments
  **/
  @ApiModelProperty(required = true, value = "")
  public List<EquipmentsEnum> getEquipments() {
    return equipments;
  }

  public void setEquipments(List<EquipmentsEnum> equipments) {
    this.equipments = equipments;
  }

  public VJDisplayInformation headsigns(List<String> headsigns) {
    this.headsigns = headsigns;
    return this;
  }

  public VJDisplayInformation addHeadsignsItem(String headsignsItem) {
    if (this.headsigns == null) {
      this.headsigns = new ArrayList<String>();
    }
    this.headsigns.add(headsignsItem);
    return this;
  }

   /**
   * Get headsigns
   * @return headsigns
  **/
  @ApiModelProperty(value = "")
  public List<String> getHeadsigns() {
    return headsigns;
  }

  public void setHeadsigns(List<String> headsigns) {
    this.headsigns = headsigns;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VJDisplayInformation vjDisplayInformation = (VJDisplayInformation) o;
    return Objects.equals(this.commercialMode, vjDisplayInformation.commercialMode) &&
        Objects.equals(this.network, vjDisplayInformation.network) &&
        Objects.equals(this.direction, vjDisplayInformation.direction) &&
        Objects.equals(this.label, vjDisplayInformation.label) &&
        Objects.equals(this.color, vjDisplayInformation.color) &&
        Objects.equals(this.code, vjDisplayInformation.code) &&
        Objects.equals(this.headsign, vjDisplayInformation.headsign) &&
        Objects.equals(this.name, vjDisplayInformation.name) &&
        Objects.equals(this.links, vjDisplayInformation.links) &&
        Objects.equals(this.textColor, vjDisplayInformation.textColor) &&
        Objects.equals(this.tripShortName, vjDisplayInformation.tripShortName) &&
        Objects.equals(this.description, vjDisplayInformation.description) &&
        Objects.equals(this.physicalMode, vjDisplayInformation.physicalMode) &&
        Objects.equals(this.equipments, vjDisplayInformation.equipments) &&
        Objects.equals(this.headsigns, vjDisplayInformation.headsigns);
  }

  @Override
  public int hashCode() {
    return Objects.hash(commercialMode, network, direction, label, color, code, headsign, name, links, textColor, tripShortName, description, physicalMode, equipments, headsigns);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VJDisplayInformation {\n");

    sb.append("    commercialMode: ").append(toIndentedString(commercialMode)).append("\n");
    sb.append("    network: ").append(toIndentedString(network)).append("\n");
    sb.append("    direction: ").append(toIndentedString(direction)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    color: ").append(toIndentedString(color)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    headsign: ").append(toIndentedString(headsign)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    textColor: ").append(toIndentedString(textColor)).append("\n");
    sb.append("    tripShortName: ").append(toIndentedString(tripShortName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    physicalMode: ").append(toIndentedString(physicalMode)).append("\n");
    sb.append("    equipments: ").append(toIndentedString(equipments)).append("\n");
    sb.append("    headsigns: ").append(toIndentedString(headsigns)).append("\n");
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

