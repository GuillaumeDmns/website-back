package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum BoardingPositionEnum {
    @JsonProperty("front")
    FRONT("front"),
    @JsonProperty("middle")
    MIDDLE("middle"),
    @JsonProperty("back")
    BACK("back");


    private String value;

    BoardingPositionEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static BoardingPositionEnum fromValue(String text) {
        for (BoardingPositionEnum b : BoardingPositionEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
