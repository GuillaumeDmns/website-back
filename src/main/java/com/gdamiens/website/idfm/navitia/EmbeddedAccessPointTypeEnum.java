package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EmbeddedAccessPointTypeEnum {
    @JsonProperty("pt_access_point")
    PT_ACCESS_POINT("pt_access_point"),

    @JsonProperty("poi_access_point")
    POI_ACCESS_POINT("poi_access_point");

    private String value;

    EmbeddedAccessPointTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static EmbeddedAccessPointTypeEnum fromValue(String text) {
        for (EmbeddedAccessPointTypeEnum b : EmbeddedAccessPointTypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
