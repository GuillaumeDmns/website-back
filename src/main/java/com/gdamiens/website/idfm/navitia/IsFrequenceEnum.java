package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum IsFrequenceEnum {
    @JsonProperty("False")
    FALSE("False");

    private String value;

    IsFrequenceEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static IsFrequenceEnum fromValue(String text) {
        for (IsFrequenceEnum b : IsFrequenceEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
