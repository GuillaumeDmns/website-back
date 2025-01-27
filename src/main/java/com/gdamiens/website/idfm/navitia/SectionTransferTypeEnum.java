package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SectionTransferTypeEnum {
    @JsonProperty("walking")
    WALKING("walking"),
    @JsonProperty("stay_in")
    STAY_IN("stay_in");

    private String value;

    SectionTransferTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SectionTransferTypeEnum fromValue(String text) {
        for (SectionTransferTypeEnum b : SectionTransferTypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
