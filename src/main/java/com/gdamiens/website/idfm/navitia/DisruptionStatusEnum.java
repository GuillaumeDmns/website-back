package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DisruptionStatusEnum {

    @JsonProperty("past")
    PAST("past"),

    @JsonProperty("active")
    FUTURE("active"),

    @JsonProperty("future")
    OPEN("future");

    private String value;

    DisruptionStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static DisruptionStatusEnum fromValue(String text) {
        for (DisruptionStatusEnum b : DisruptionStatusEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
