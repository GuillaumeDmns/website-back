package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StopTimeEffectEnum {

    @JsonProperty("delayed")
    DELAYED("delayed"),

    @JsonProperty("added")
    ADDED("added"),

    @JsonProperty("deleted")
    DELETED("deleted"),

    @JsonProperty("unchanged")
    UNCHANGED("unchanged");

    private String value;

    StopTimeEffectEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static StopTimeEffectEnum fromValue(String text) {
        for (StopTimeEffectEnum b : StopTimeEffectEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
