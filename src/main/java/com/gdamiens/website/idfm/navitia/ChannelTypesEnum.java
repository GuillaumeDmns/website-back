package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ChannelTypesEnum {

    @JsonProperty("web")
    WEB("web"),

    @JsonProperty("sms")
    SMS("sms"),

    @JsonProperty("email")
    EMAIL("email"),

    @JsonProperty("mobile")
    MOBILE("mobile"),

    @JsonProperty("notification")
    NOTIFICATION("notification"),

    @JsonProperty("twitter")
    TWITTER("twitter"),

    @JsonProperty("facebook")
    FACEBOOK("facebook"),

    @JsonProperty("unknown_type")
    UNKNOWN_TYPE("unknown_type"),

    @JsonProperty("title")
    TITLE("title"),

    @JsonProperty("beacon")
    BEACON("beacon"),

    @JsonProperty("pids")
    PIDS("pids");

    private String value;

    ChannelTypesEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static ChannelTypesEnum fromValue(String text) {
        for (ChannelTypesEnum b : ChannelTypesEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
