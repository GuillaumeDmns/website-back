package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EmbeddedEquipmentDetailsTypeEnum {
    @JsonProperty("escalator")
    ESCALATOR("escalator"),

    @JsonProperty("elevator")
    ELEVATOR("elevator");

    private String value;

    EmbeddedEquipmentDetailsTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static EmbeddedEquipmentDetailsTypeEnum fromValue(String text) {
        for (EmbeddedEquipmentDetailsTypeEnum b : EmbeddedEquipmentDetailsTypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
