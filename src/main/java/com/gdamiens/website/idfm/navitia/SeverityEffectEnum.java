package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SeverityEffectEnum {
    @JsonProperty("NO_SERVICE")
    NO_SERVICE("NO_SERVICE"),

    @JsonProperty("REDUCED_SERVICE")
    REDUCED_SERVICE("REDUCED_SERVICE"),

    @JsonProperty("SIGNIFICANT_DELAYS")
    SIGNIFICANT_DELAYS("SIGNIFICANT_DELAYS"),

    @JsonProperty("DETOUR")
    DETOUR("DETOUR"),

    @JsonProperty("ADDITIONAL_SERVICE")
    ADDITIONAL_SERVICE("ADDITIONAL_SERVICE"),

    @JsonProperty("MODIFIED_SERVICE")
    MODIFIED_SERVICE("MODIFIED_SERVICE"),

    @JsonProperty("OTHER_EFFECT")
    OTHER_EFFECT("OTHER_EFFECT"),

    @JsonProperty("UNKNOWN_EFFECT")
    UNKNOWN_EFFECT("UNKNOWN_EFFECT"),

    @JsonProperty("STOP_MOVED")
    STOP_MOVED("STOP_MOVED");

    private String value;

    SeverityEffectEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SeverityEffectEnum fromValue(String text) {
        for (SeverityEffectEnum b : SeverityEffectEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
