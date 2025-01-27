package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SectionModeEnum {
    @JsonProperty("walking")
    WALKING("walking"),
    @JsonProperty("bike")
    BIKE("bike"),
    @JsonProperty("car")
    CAR("car"),
    @JsonProperty("bss")
    BSS("bss"),
    @JsonProperty("ridesharing")
    RIDESHARING("ridesharing"),
    @JsonProperty("carnopark")
    CARNOPARK("carnopark"),
    @JsonProperty("taxi")
    TAXI("taxi");

    private String value;

    SectionModeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SectionModeEnum fromValue(String text) {
        for (SectionModeEnum b : SectionModeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
