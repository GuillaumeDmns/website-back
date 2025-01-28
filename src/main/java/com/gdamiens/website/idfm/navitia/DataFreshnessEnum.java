package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DataFreshnessEnum {
    @JsonProperty("base_schedule")
    BASE_SCHEDULE("base_schedule"),

    @JsonProperty("adapted_schedule")
    ADAPTED_SCHEDULE("adapted_schedule"),

    @JsonProperty("realtime")
    REALTIME("realtime");

    private String value;

    DataFreshnessEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static DataFreshnessEnum fromValue(String text) {
        for (DataFreshnessEnum b : DataFreshnessEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
