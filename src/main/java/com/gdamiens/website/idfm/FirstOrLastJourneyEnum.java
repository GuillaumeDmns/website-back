package com.gdamiens.website.idfm;


import com.fasterxml.jackson.annotation.JsonProperty;

public enum FirstOrLastJourneyEnum {
    @JsonProperty("unspecified")
    UNSPECIFIED("unspecified"),

    @JsonProperty("firstServiceOfDay")
    FIRST_SERVICE_OF_DAY("firstServiceOfDay"),

    @JsonProperty("lastServiceOfDay")
    LAST_SERVICE_OF_DAY("lastServiceOfDay"),

    @JsonProperty("otherService")
    OTHER_SERVICE("otherService");

    public final String value;

    FirstOrLastJourneyEnum(String value) {
        this.value = value;
    }
}
