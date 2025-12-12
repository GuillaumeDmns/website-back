package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StopTimeEffectEnum {

    @JsonProperty("DELAYED")
    DELAYED("delayed"),

    @JsonProperty("ADDED")
    ADDED("added"),

    @JsonProperty("DELETED")
    DELETED("deleted"),

    @JsonProperty("UNCHANGED")
    UNCHANGED("unchanged"),

    @JsonProperty("NO_ALIGHTING")
    NO_ALIGHTING("no_alighting"),

    @JsonProperty("NO_BOARDING")
    NO_BOARDING("no_boarding");

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
