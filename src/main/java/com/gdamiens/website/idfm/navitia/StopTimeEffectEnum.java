package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum StopTimeEffectEnum {

    @JsonProperty("DELAYED")
    @JsonAlias("delayed")
    DELAYED("delayed"),

    @JsonProperty("ADDED")
    @JsonAlias("added")
    ADDED("added"),

    @JsonProperty("DELETED")
    @JsonAlias("deleted")
    DELETED("deleted"),

    @JsonProperty("UNCHANGED")
    @JsonAlias("unchanged")
    UNCHANGED("unchanged"),

    @JsonProperty("NO_ALIGHTING")
    @JsonAlias("unchno_alightinganged")
    NO_ALIGHTING("no_alighting"),

    @JsonProperty("NO_BOARDING")
    @JsonAlias("no_boarding")
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
