package com.gdamiens.website.idfm;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StatusEnum {
    @JsonProperty("onTime")
    ON_TIME("onTime"),

    @JsonProperty("early")
    EARLY("early"),

    @JsonProperty("missed")
    MISSED("missed"),

    @JsonProperty("delayed")
    DELAYED("delayed"),

    @JsonProperty("cancelled")
    CANCELLED("cancelled"),

    @JsonProperty("arrived")
    ARRIVED("arrived"),

    @JsonProperty("departed")
    DEPARTED("departed"),

    @JsonProperty("notExpected")
    NOT_EXPECTED("notExpected"),

    @JsonProperty("noReport")
    NO_REPORT("noReport");

    public final String value;

    StatusEnum(String value) {
        this.value = value;
    }
}
