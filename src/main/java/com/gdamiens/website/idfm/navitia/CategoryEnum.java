package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CategoryEnum {
    @JsonProperty("comment")
    COMMENT("comment"),

    @JsonProperty("terminus")
    TERMINUS("terminus");

    private String value;

    CategoryEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static CategoryEnum fromValue(String text) {
        for (CategoryEnum b : CategoryEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
