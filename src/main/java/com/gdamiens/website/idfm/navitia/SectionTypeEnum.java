package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SectionTypeEnum {
    @JsonProperty("public_transport")
    PUBLIC_TRANSPORT("public_transport"),
    @JsonProperty("street_network")
    STREET_NETWORK("street_network"),
    @JsonProperty("waiting")
    WAITING("waiting"),
    @JsonProperty("transfer")
    TRANSFER("transfer"),
    @JsonProperty("boarding")
    BOARDING("boarding"),
    @JsonProperty("landing")
    LANDING("landing"),
    @JsonProperty("bss_rent")
    BSS_RENT("bss_rent"),
    @JsonProperty("bss_put_back")
    BSS_PUT_BACK("bss_put_back"),
    @JsonProperty("crow_fly")
    CROW_FLY("crow_fly"),
    @JsonProperty("park")
    PARK("park"),
    @JsonProperty("leave_parking")
    LEAVE_PARKING("leave_parking"),
    @JsonProperty("alighting")
    ALIGHTING("alighting"),
    @JsonProperty("ridesharing")
    RIDESHARING("ridesharing"),
    @JsonProperty("on_demand_transport")
    ON_DEMAND_TRANSPORT("on_demand_transport");

    private String value;

    SectionTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SectionTypeEnum fromValue(String text) {
        for (SectionTypeEnum b : SectionTypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
