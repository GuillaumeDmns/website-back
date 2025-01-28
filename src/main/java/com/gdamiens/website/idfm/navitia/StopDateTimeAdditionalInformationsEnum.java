package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StopDateTimeAdditionalInformationsEnum {
    @JsonProperty("pick_up_only")
    PICK_UP_ONLY("pick_up_only"),

    @JsonProperty("drop_off_only")
    DROP_OFF_ONLY("drop_off_only"),

    @JsonProperty("on_demand_transport")
    ON_DEMAND_TRANSPORT("on_demand_transport"),

    @JsonProperty("date_time_estimated")
    DATE_TIME_ESTIMATED("date_time_estimated");

    private String value;

    StopDateTimeAdditionalInformationsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static StopDateTimeAdditionalInformationsEnum fromValue(String text) {
        for (StopDateTimeAdditionalInformationsEnum b : StopDateTimeAdditionalInformationsEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
