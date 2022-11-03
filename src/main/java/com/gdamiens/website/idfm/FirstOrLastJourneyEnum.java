package com.gdamiens.website.idfm;


public enum FirstOrLastJourneyEnum {
    UNSPECIFIED("unspecified"),
    FIRST_SERVICE_OF_DAY("firstServiceOfDay"),
    LAST_SERVICE_OF_DAY("lastServiceOfDay"),
    OTHER_SERVICE("otherService");

    public final String value;

    FirstOrLastJourneyEnum(String value) {
        this.value = value;
    }
}
