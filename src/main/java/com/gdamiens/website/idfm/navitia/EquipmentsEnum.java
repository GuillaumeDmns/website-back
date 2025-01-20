package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EquipmentsEnum {
    @JsonProperty("has_wheelchair_accessibility")
    WHEELCHAIR_ACCESSIBILITY("has_wheelchair_accessibility"),

    @JsonProperty("has_bike_accepted")
    BIKE_ACCEPTED("has_bike_accepted"),

    @JsonProperty("has_air_conditioned")
    AIR_CONDITIONED("has_air_conditioned"),

    @JsonProperty("has_visual_announcement")
    VISUAL_ANNOUNCEMENT("has_visual_announcement"),

    @JsonProperty("has_audible_announcement")
    AUDIBLE_ANNOUNCEMENT("has_audible_announcement"),

    @JsonProperty("has_appropriate_escort")
    APPROPRIATE_ESCORT("has_appropriate_escort"),

    @JsonProperty("has_appropriate_signage")
    APPROPRIATE_SIGNAGE("has_appropriate_signage"),

    @JsonProperty("has_school_vehicle")
    SCHOOL_VEHICLE("has_school_vehicle"),

    @JsonProperty("has_wheelchair_boarding")
    WHEELCHAIR_BOARDING("has_wheelchair_boarding"),

    @JsonProperty("has_sheltered")
    SHELTERED("has_sheltered"),

    @JsonProperty("has_elevator")
    ELEVATOR("has_elevator"),

    @JsonProperty("has_escalator")
    ESCALATOR("has_escalator"),

    @JsonProperty("has_bike_depot")
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
}
