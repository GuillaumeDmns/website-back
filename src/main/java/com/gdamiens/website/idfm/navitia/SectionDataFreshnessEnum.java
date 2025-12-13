package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SectionDataFreshnessEnum {
    @JsonProperty("base_schedule")
    BASE_SCHEDULE("base_schedule"),
    @JsonProperty("adapted_schedule")
    ADAPTED_SCHEDULE("adapted_schedule"),
    @JsonProperty("realtime")
    REALTIME("realtime");

    private String value;

    SectionDataFreshnessEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SectionDataFreshnessEnum fromValue(String text) {
        for (SectionDataFreshnessEnum b : SectionDataFreshnessEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
