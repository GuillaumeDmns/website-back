package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EmbeddedTypeEnum {
    @JsonProperty("line")
    LINE("line"),

    @JsonProperty("journey_pattern")
    JOURNEY_PATTERN("journey_pattern"),

    @JsonProperty("vehicle_journey")
    VEHICLE_JOURNEY("vehicle_journey"),

    @JsonProperty("stop_point")
    STOP_POINT("stop_point"),

    @JsonProperty("stop_area")
    STOP_AREA("stop_area"),

    @JsonProperty("network")
    NETWORK("network"),

    @JsonProperty("physical_mode")
    PHYSICAL_MODE("physical_mode"),

    @JsonProperty("commercial_mode")
    COMMERCIAL_MODE("commercial_mode"),

    @JsonProperty("connection")
    CONNECTION("connection"),

    @JsonProperty("journey_pattern_point")
    JOURNEY_PATTERN_POINT("journey_pattern_point"),

    @JsonProperty("company")
    COMPANY("company"),

    @JsonProperty("route")
    ROUTE("route"),

    @JsonProperty("poi")
    POI("poi"),

    @JsonProperty("contributor")
    CONTRIBUTOR("contributor"),

    @JsonProperty("address")
    ADDRESS("address"),

    @JsonProperty("poitype")
    POITYPE("poitype"),

    @JsonProperty("administrative_region")
    ADMINISTRATIVE_REGION("administrative_region"),

    @JsonProperty("calendar")
    CALENDAR("calendar"),

    @JsonProperty("line_group")
    LINE_GROUP("line_group"),

    @JsonProperty("impact")
    IMPACT("impact"),

    @JsonProperty("dataset")
    DATASET("dataset"),

    @JsonProperty("trip")
    TRIP("trip"),

    @JsonProperty("access_point")
    ACCESS_POINT("access_point");

    private String value;

    EmbeddedTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static EmbeddedTypeEnum fromValue(String text) {
        for (EmbeddedTypeEnum b : EmbeddedTypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
