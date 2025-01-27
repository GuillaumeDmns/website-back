package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SectionAdditionalInformationsEnum {
    @JsonProperty("odt_with_zone")
    ODT_WITH_ZONE("odt_with_zone"),
    @JsonProperty("odt_with_stop_point")
    ODT_WITH_STOP_POINT("odt_with_stop_point"),
    @JsonProperty("odt_with_stop_time")
    ODT_WITH_STOP_TIME("odt_with_stop_time"),
    @JsonProperty("has_datetime_estimated")
    HAS_DATETIME_ESTIMATED("has_datetime_estimated"),
    @JsonProperty("regular")
    REGULAR("regular"),
    @JsonProperty("stay_in")
    STAY_IN("stay_in");

    private String value;

    SectionAdditionalInformationsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SectionAdditionalInformationsEnum fromValue(String text) {
        for (SectionAdditionalInformationsEnum b : SectionAdditionalInformationsEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
